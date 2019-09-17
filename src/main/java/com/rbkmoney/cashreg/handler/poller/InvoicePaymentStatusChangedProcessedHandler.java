package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.cashreg.CashRegDeliveryService;
import com.rbkmoney.cashreg.service.cashreg.InvoicePayerService;
import com.rbkmoney.cashreg.service.cashreg.PaymentService;
import com.rbkmoney.cashreg.utils.constant.CashRegStatus;
import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedProcessedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final CashRegDeliveryService cashRegDeliveryService;

    public void handle(InvoiceChange ic, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        log.info("Start {} with paymentId {}.{}", handlerEvent, invoiceId, paymentId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentId(invoiceId, paymentId);
        if (invoicePayer == null) {
            log.info("InvoicePayer is missing, payment {}.{}", invoiceId, paymentId);
            return;
        }

        Payment payment = invoicePayer.getPayment();
        if (!PaymentStatus.STARTED.equals(payment.getStatus())) {
            log.info("Duplicate found, payment: {}.{}", invoiceId, paymentId);
            return;
        }

        payment.setStatus(PaymentStatus.PROCESSED);
        Payment paymentDB = paymentService.save(payment);

        cashRegDeliveryService.save(CashRegDelivery.builder()
                .invoiceId(invoicePayer)
                .paymentId(paymentDB)
                .typeOperation(CashRegTypeOperation.DEBIT)
                .cashregStatus(CashRegStatus.READY)
                .build()
        );

        log.info("End {} with invoice_id {}, paymentId {}",
                handlerEvent, invoiceId, paymentId
        );
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STATUS_CHANGED_PROCESSED;
    }
}
