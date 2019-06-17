package com.rbkmoney.cashreg.handler.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.entity.Refund;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.InvoicePayerService;
import com.rbkmoney.cashreg.service.PaymentService;
import com.rbkmoney.cashreg.service.RefundService;
import com.rbkmoney.cashreg.utils.constant.RefundStatus;
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.cashreg.utils.cart.CartUtils.prepareCartInvoiceLine;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundCreatedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(InvoiceChange ic, MachineEvent event, String sourceId) {
        Invoice invoice = ic.getInvoiceCreated().getInvoice();
        String invoiceId = invoice.getId();
        String paymentId = ic.getInvoicePaymentChange().getId();
        String refundId = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId();
        log.info("Start {} with invoice_id {}, paymentId {}, refundId {}", handlerEvent, invoiceId, paymentId, refundId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(invoiceId, paymentId);
        if (invoicePayer == null) {
            log.debug("InvoicePayer is missing, invoiceId {}", invoiceId);
            return;
        }

        Long amount = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundCreated().getRefund().getCash().getAmount();

        Refund refund = new Refund();
        refund.setAmount(amount);
        refund.setRefundId(refundId);
        refund.setStatus(RefundStatus.CREATED);

        List<InvoiceLine> lineList = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundCreated().getRefund().getCart().getLines();
        if (!lineList.isEmpty()) {
            try {
                String lines = prepareCartInvoiceLine(objectMapper, lineList);
                refund.setCart(lines);
                refund.setPreviousCart(invoicePayer.getExchangeCart());
            } catch (JsonProcessingException e) {
                log.debug("{}: InvoicePayer cart lines is empty", handlerEvent);
            }
        }

        Refund refundDB = refundService.save(refund);

        if (refundDB == null) {
            log.debug("Couldn't save Refund. payment {}.{}", invoiceId, paymentId);
            return;
        } else {
            log.debug("Saved Refund. payment {}.{}", invoiceId, paymentId);
        }

        Payment payment = invoicePayer.getPayment();
        payment.setRefund(refundDB);

        Payment paymentDB = paymentService.save(payment);
        if (paymentDB == null) {
            log.debug("Couldn't save Payment. payment {}.{}", invoiceId, paymentId);
            return;
        } else {
            log.debug("Saved Payment. payment {}.{}", invoiceId, paymentId);
        }

        // Если есть корзина, то обновляем обменную корзину
        if(refundDB.getCart() != null) {
            invoicePayer.setExchangeCart(refundDB.getCart());
        }
        InvoicePayer invoicePayerUpdate = invoicePayerService.save(invoicePayer);
        if (invoicePayerUpdate == null) {
            log.debug("{}: InvoicePayer not save", handlerEvent);
        } else {
            log.debug("Saved InvoicePayer. payment {}.{}", invoiceId, paymentId);
        }

        log.info("End {} with invoice_id {}, paymentId {}, refundId {}",
                handlerEvent, invoiceId, paymentId, refundId
        );
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_CREATED;
    }
}
