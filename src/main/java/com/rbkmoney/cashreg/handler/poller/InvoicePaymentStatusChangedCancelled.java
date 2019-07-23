package com.rbkmoney.cashreg.handler.poller;


import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.CashRegDeliveryService;
import com.rbkmoney.cashreg.service.InvoicePayerService;
import com.rbkmoney.cashreg.service.PaymentService;
import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedCancelled implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final CashRegDeliveryService cashRegDeliveryService;

    @Override
    public void handle(InvoiceChange ic, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        log.info("Start {} with payment {}.{}", handlerEvent, invoiceId, paymentId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(invoiceId, paymentId);
        if (invoicePayer == null) {
            log.info("InvoicePayer is missing, payment {}.{}", invoiceId, paymentId);
            return;
        }

        Payment payment = invoicePayer.getPayment();

        if (!PaymentStatus.PROCESSED.equals(payment.getStatus())) {
            log.info("Duplicate found, payment: {}.{}", invoiceId, paymentId);
            return;
        }

        payment.setStatus(PaymentStatus.CANCELLED);

        Payment paymentDB = paymentService.save(payment);

        cashRegDeliveryService.createRefundCashRegIfExists(invoicePayer, paymentDB, CashRegTypeOperation.DEBIT);

        log.info("End {} with paymentId {}.{}",
                handlerEvent, invoiceId, paymentId);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STATUS_CHANGED_CANCELLED;
    }
}
