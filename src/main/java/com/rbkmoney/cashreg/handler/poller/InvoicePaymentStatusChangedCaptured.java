package com.rbkmoney.cashreg.handler.poller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.CashRegDeliveryService;
import com.rbkmoney.cashreg.service.InvoicePayerService;
import com.rbkmoney.cashreg.service.PaymentService;
import com.rbkmoney.cashreg.utils.constant.CartState;
import com.rbkmoney.cashreg.utils.constant.CashRegStatus;
import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.cashreg.utils.cart.CartUtils.prepareCartInvoiceLine;
import static com.rbkmoney.cashreg.utils.cart.CartUtils.prepareCashRegDelivery;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedCaptured implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final CashRegDeliveryService cashRegDeliveryService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(InvoiceChange ic, MachineEvent event, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        log.info("Start {} with payment {}.{}", handlerEvent, invoiceId, paymentId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(invoiceId, paymentId);
        if (invoicePayer == null) {
            log.debug("{}: InvoicePayer is missing, invoiceId {}", handlerEvent, invoiceId);
            return;
        }

        InvoicePaymentCaptured invocePaymentCaptured = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus().getCaptured();

        Payment payment = invoicePayer.getPayment();
        payment.setStatus(PaymentStatus.CAPTURED);
        Long partialAmount = invocePaymentCaptured.getCost().getAmount();
        payment.setPartialAmount(partialAmount);

        // Если корзины нет, то ничего не делаем
        if (!invocePaymentCaptured.isSetCart()) {
            return;
        }

        List<InvoiceLine> lineList = invocePaymentCaptured.getCart().getLines();
        if (!lineList.isEmpty()) {
            try {
                String lines = prepareCartInvoiceLine(objectMapper, lineList);
                payment.setCaptureCart(lines);
            } catch (JsonProcessingException e) {
                log.debug("{}: InvoicePayer cart lines is empty", handlerEvent);
            }
        }


        Payment paymentDB = paymentService.save(payment);
        if (paymentDB == null) {
            log.debug("{}: couldn't save Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
            return;
        } else {
            log.debug("{}: saved Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
        }

        // отправляем чек возврата-прихода c корзиной инвойса
        CashRegDelivery cashRegDeliveryCheck = cashRegDeliveryService.findByTypeOperationAndCashregStatus(
                invoicePayer, paymentDB, CashRegTypeOperation.REFUND_DEBIT
        );

        if (cashRegDeliveryCheck != null) {
            CashRegDelivery cashRegDelivery = prepareCashRegDelivery(
                    invoicePayer, paymentDB, null,
                    CashRegTypeOperation.REFUND_DEBIT,
                    CashRegStatus.READY,
                    CartState.FULL
            );

            CashRegDelivery cashRegDeliveryDB = cashRegDeliveryService.save(cashRegDelivery);
            if (cashRegDeliveryDB == null) {
                log.debug("{}: couldn't save CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
                return;
            } else {
                log.debug("{}: saved CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
            }
        }

        // отправляем чек прихода с корзиной payment captured
        cashRegDeliveryCheck = cashRegDeliveryService.findByTypeOperationAndCashregStatus(
                invoicePayer, paymentDB, CashRegTypeOperation.DEBIT
        );

        if (cashRegDeliveryCheck != null) {
            CashRegDelivery cashRegDelivery = prepareCashRegDelivery(
                    invoicePayer, paymentDB, null,
                    CashRegTypeOperation.DEBIT,
                    CashRegStatus.READY,
                    CartState.PARTIAL
            );

            CashRegDelivery cashRegDeliveryDB = cashRegDeliveryService.save(cashRegDelivery);
            if (cashRegDeliveryDB == null) {
                log.debug("{}: couldn't save CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
                return;
            } else {
                log.debug("{}: saved CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
            }
        }

        log.info("End {} with invoice_id {}, paymentId {}", handlerEvent, invoiceId, paymentId);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STATUS_CHANGED_CAPTURED;
    }
}
