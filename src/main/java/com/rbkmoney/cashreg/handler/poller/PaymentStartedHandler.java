package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.entity.*;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.*;
import com.rbkmoney.cashreg.utils.constant.PaymentStatus;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStartedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final PayerInfoService payerInfoService;
    private final ContactTypeService contactTypeService;
    private final PaymentTypeService paymentTypeService;

    @Override
    @Transactional
    public void handle(InvoiceChange ic, MachineEvent event, String invoiceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        log.info("Start {}: payment {}.{}", handlerEvent, invoiceId, paymentId);

        InvoicePayment invoicePayment = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentStarted().getPayment();
        Payer payer = invoicePayment.getPayer();

        boolean isContactInfo;
        boolean isEmail = false;
        if (payer.isSetPaymentResource()) {
            isContactInfo = payer.getPaymentResource().isSetContactInfo();
            if (isContactInfo) {
                isEmail = payer.getPaymentResource().getContactInfo().isSetEmail();
            }
        } else if (payer.isSetCustomer()) {
            isContactInfo = payer.getCustomer().isSetContactInfo();
            if (isContactInfo) {
                isEmail = payer.getCustomer().getContactInfo().isSetEmail();
            }
        } else {
            isContactInfo = payer.getRecurrent().isSetContactInfo();
            if (isContactInfo) {
                isEmail = payer.getRecurrent().getContactInfo().isSetEmail();
            }
        }

        if (isContactInfo && isEmail) {
            InvoicePayer invoicePayer = invoicePayerService.findByInvoiceId(invoiceId);
            if (invoicePayer == null) {
                log.debug("{}: couldn't find invoicePayer. payment {}.{}",
                        handlerEvent, invoiceId, paymentId
                );
                return;
            }


            ContactType contactType = contactTypeService.findByContactType(ContactType.EMAIL);
            if (contactType == null) {

                contactType = contactTypeService.save(new ContactType(ContactType.EMAIL));
                if (contactType == null) {
                    log.debug("{}: couldn't save contactType.  payment {}.{}",
                            handlerEvent, invoiceId, paymentId
                    );
                    return;
                } else {
                    log.debug("{}: saved contactType.  payment {}.{}", handlerEvent, invoiceId, paymentId);
                }
            }

            // Now it's work only for email
            String contactInfo = extractContactInfo(payer);
            PayerInfo payerInfo = payerInfoService.findByContact(contactInfo);

            if (payerInfo == null) {
                payerInfo = payerInfoService.save(new PayerInfo(contactInfo, contactType));
                if (payerInfo == null) {
                    log.debug("{}: couldn't save payerInfo. payment {}.{}", handlerEvent, invoiceId, paymentId);
                    return;
                } else {
                    log.debug("{}: saved payerInfo.  payment {}.{}", handlerEvent, invoiceId, paymentId);
                }

            }

            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setAmountOrig(invoicePayment.getCost().getAmount());
            payment.setPayerInfo(payerInfo);
            payment.setStatus(PaymentStatus.STARTED);

            String currentPaymentType;
            if (payer.isSetPaymentResource()) {
                currentPaymentType = payer.getPaymentResource().getResource().getPaymentTool().getSetField().getFieldName();
            } else {
                currentPaymentType = payer.getCustomer().getPaymentTool().getSetField().getFieldName();
            }

            PaymentType paymentType = paymentTypeService.findByType(currentPaymentType);
            if (paymentType == null) {
                paymentType = paymentTypeService.save(new PaymentType(currentPaymentType));
                if (paymentType == null) {
                    log.debug("{}: couldn't save paymentType. payment {}.{}", handlerEvent, invoiceId, paymentId);
                    return;
                } else {
                    log.debug("{}: saved paymentType. payment {}.{}", handlerEvent, invoiceId, paymentId);
                }

            }

            payment.setPaymentType(paymentType);
            Payment paymentDB = paymentService.save(payment);

            if (paymentDB == null) {
                log.debug("{}: couldn't save Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
                return;
            } else {
                log.debug("{}: saved Payment. payment {}.{}", handlerEvent, invoiceId, paymentId);
            }


            invoicePayer.setPayment(paymentDB);
            InvoicePayer invoicePayerDB = invoicePayerService.save(invoicePayer);
            if (invoicePayerDB == null) {
                log.debug("{}: couldn't save invoicePayer. payment {}.{}",
                        handlerEvent, invoiceId, paymentId
                );
                return;
            } else {
                log.debug("{}: saved invoicePayer. payment {}.{}",
                        handlerEvent, invoiceId, paymentId
                );
            }
        }

        log.info("End {}: payment {}.{}", handlerEvent, invoiceId, paymentId);
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

}
