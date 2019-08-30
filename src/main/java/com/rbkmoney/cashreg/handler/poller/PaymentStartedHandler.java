package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.entity.*;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.*;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStartedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final PayerInfoService payerInfoService;


    @Transactional
    public void handle(InvoiceChange ic, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        log.info("Start {}: payment {}.{}", handlerEvent, invoiceId, paymentId);

        InvoicePayment invoicePayment = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStarted().getPayment();
        Payer payer = invoicePayment.getPayer();

        String email = getEmail(payer);
        if (!StringUtils.isEmpty(email)) {
            InvoicePayer invoicePayer = invoicePayerService.findByInvoiceId(invoiceId);

            if (invoicePayer == null) {
                log.info("InvoicePayer is missing, payment {}.{}", invoiceId, paymentId);
                return;
            }

            if (invoicePayer.getPayment() != null
                    && (invoicePayer.getPayment().getPaymentId().equals(paymentId)
                    || Integer.parseInt(paymentId) < Integer.parseInt(invoicePayer.getPayment().getPaymentId()))) {
                log.info("Duplicate found, payment: {}.{}", invoiceId, paymentId);
                return;
            }

            ContactType contactType = ContactType.findByType(ContactType.EMAIL.getType());

            // Now it's work only for email
            String contactInfo = extractContactInfo(payer);
            PayerInfo payerInfo = payerInfoService.findByContact(contactInfo);
            if (payerInfo == null) {
                payerInfo = payerInfoService.save(PayerInfo.builder()
                        .contact(contactInfo)
                        .contactType(contactType.getType())
                        .build()
                );
            }

            PaymentType paymentType = PaymentType.findByType(extractPaymentType(payer));
            Payment paymentDB = paymentService.save(Payment.builder().paymentId(paymentId)
                    .amountOrig(invoicePayment.getCost().getAmount())
                    .payerInfo(payerInfo)
                    .status(PaymentStatus.STARTED)
                    .currency(invoicePayment.getCost().getCurrency().getSymbolicCode())
                    .paymentType(paymentType.getType()).build());


            invoicePayer.setPayment(paymentDB);
            invoicePayerService.save(invoicePayer);
        } else {
            log.info("Received payment with empty email, payment: {}.{}", invoiceId, paymentId);
        }

        log.info("End {}: payment {}.{}", handlerEvent, invoiceId, paymentId);
    }

    private String extractPaymentType(Payer payer) {
        String currentPaymentType;
        if (payer.isSetPaymentResource()) {
            currentPaymentType = payer.getPaymentResource().getResource().getPaymentTool().getSetField().getFieldName();
        } else if (payer.isSetCustomer()) {
            currentPaymentType = payer.getCustomer().getPaymentTool().getSetField().getFieldName();
        } else {
            currentPaymentType = payer.getRecurrent().getPaymentTool().getSetField().getFieldName();
        }
        return currentPaymentType;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STARTED;
    }

    private String extractContactInfo(Payer payer) {
        String email;
        if (payer.isSetPaymentResource()) {
            email = payer.getPaymentResource().getContactInfo().getEmail();
        } else if (payer.isSetCustomer()) {
            email = payer.getCustomer().getContactInfo().getEmail();
        } else {
            email = payer.getRecurrent().getContactInfo().getEmail();
        }
        return email;
    }

    private String getEmail(Payer payer) {
        if (payer.isSetPaymentResource()
                && payer.getPaymentResource().isSetContactInfo()
                && payer.getPaymentResource().getContactInfo().isSetEmail()) {
            return payer.getPaymentResource().getContactInfo().getEmail();

        } else if (payer.isSetCustomer()
                && payer.getCustomer().isSetContactInfo()
                && payer.getCustomer().getContactInfo().isSetEmail()) {
            return payer.getCustomer().getContactInfo().getEmail();
        } else if (payer.isSetRecurrent()
                && payer.getRecurrent().isSetContactInfo()
                && payer.getRecurrent().getContactInfo().isSetEmail()) {
            return payer.getRecurrent().getContactInfo().getEmail();
        }
        return null;
    }

}
