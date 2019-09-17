package com.rbkmoney.cashreg.handler.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.entity.Refund;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.cashreg.InvoicePayerService;
import com.rbkmoney.cashreg.service.cashreg.PaymentService;
import com.rbkmoney.cashreg.service.cashreg.RefundService;
import com.rbkmoney.cashreg.utils.constant.RefundStatus;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
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
    public void handle(InvoiceChange ic, String sourceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        String refundId = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId();
        log.info("Start {} with refundId {}.{}.{}", handlerEvent, sourceId, paymentId, refundId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(sourceId, paymentId);
        if (invoicePayer == null) {
            log.debug("InvoicePayer is missing, refundId {}.{}.{}", sourceId, paymentId, refundId);
            return;
        }

        if (invoicePayer.getPayment().getRefund() != null
                && (invoicePayer.getPayment().getRefund().getRefundId().equals(refundId)
                || Integer.parseInt(refundId) < Integer.parseInt(invoicePayer.getPayment().getRefund().getRefundId()))) {
            log.info("Duplicate found, refund: {}.{}.{}", sourceId, paymentId, refundId);
            return;
        }

        Long amount = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload()
                .getInvoicePaymentRefundCreated().getRefund().getCash().getAmount();

        Refund.RefundBuilder refund = Refund.builder()
                .amount(amount)
                .refundId(refundId)
                .status(RefundStatus.CREATED);

        List<InvoiceLine> lineList = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().getInvoicePaymentRefundCreated().getRefund().getCart().getLines();
        if (!lineList.isEmpty()) {
            try {
                String lines = prepareCartInvoiceLine(objectMapper, lineList);
                refund.cart(lines);
                refund.previousCart(invoicePayer.getExchangeCart());
            } catch (JsonProcessingException e) {
                log.debug("{}: InvoicePayer cart lines is empty", handlerEvent);
            }
        }

        Refund refundDB = refundService.save(refund.build());

        Payment payment = invoicePayer.getPayment();
        payment.setRefund(refundDB);

        paymentService.save(payment);

        // Если есть корзина, то обновляем обменную корзину
        if (refundDB.getCart() != null) {
            invoicePayer.setExchangeCart(refundDB.getCart());
        }
        invoicePayerService.save(invoicePayer);

        log.info("End {} with paymentId {}.{}.{}", handlerEvent, sourceId, paymentId, refundId);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_CREATED;
    }
}
