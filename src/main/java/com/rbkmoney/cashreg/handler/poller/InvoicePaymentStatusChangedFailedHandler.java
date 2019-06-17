package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.CashRegDeliveryService;
import com.rbkmoney.cashreg.service.InvoicePayerService;
import com.rbkmoney.cashreg.service.PaymentService;
import com.rbkmoney.cashreg.utils.constant.CashRegStatus;
import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedFailedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final CashRegDeliveryService cashRegDeliveryService;

    public void handle(InvoiceChange ic, MachineEvent event, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();

        log.info("Start {} with invoice_id {}, paymentId {}", handlerEvent, invoiceId, paymentId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(invoiceId, paymentId);
        if (invoicePayer == null) {
            log.debug("PaymentPayer is missing, invoiceId {}", invoiceId);
            return;
        }

        Payment payment = invoicePayer.getPayment();
        payment.setStatus(PaymentStatus.FAILED);

        Payment paymentDB = paymentService.save(payment);
        if (paymentDB == null) {
            log.debug("{}: couldn't save Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
            return;
        } else {
            log.debug("{}: saved Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
        }

        CashRegDelivery cashRegDeliveryCheck = cashRegDeliveryService.findByTypeOperationAndCashregStatus(
                invoicePayer, paymentDB, CashRegTypeOperation.DEBIT
        );

        if(cashRegDeliveryCheck != null) {
            CashRegDelivery cashRegDelivery = new CashRegDelivery();
            cashRegDelivery.setInvoiceId(invoicePayer);
            cashRegDelivery.setPaymentId(paymentDB);
            cashRegDelivery.setTypeOperation(CashRegTypeOperation.REFUND_DEBIT);
            cashRegDelivery.setCashregStatus(CashRegStatus.READY);

            CashRegDelivery cashRegDeliveryDB = cashRegDeliveryService.save(cashRegDelivery);
            if (cashRegDeliveryDB == null) {
                log.debug("{}: couldn't save CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
                return;
            } else {
                log.debug("{}: saved CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
            }
        }

        log.info("End {} with invoice_id {}, paymentId {}",
                handlerEvent, invoiceId, paymentId
        );
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STATUS_CHANGED_FAILED;
    }
}
