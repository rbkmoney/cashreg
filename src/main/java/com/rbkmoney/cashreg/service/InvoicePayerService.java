package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.repository.InvoicePayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InvoicePayerService {

    private InvoicePayerRepository invoicePayerRepository;

    @Autowired
    public InvoicePayerService(InvoicePayerRepository invoicePayerRepository) {
        this.invoicePayerRepository = invoicePayerRepository;
    }

    public InvoicePayer findByInvoiceId(String invoiceId) {
        log.debug("Trying to find InvoicePayer by invoiceId {} from the DB", invoiceId);
        InvoicePayer invoice = invoicePayerRepository.findByInvoiceId(invoiceId);
        log.debug("Finish findByInvoiceId method. DB returned [{}]", invoice);
        return invoice;
    }

    public InvoicePayer findByInvoiceIdAndPaymentId(String invoiceId, String paymentId) {
        log.debug("Trying to find InvoicePayer by invoiceId {}, paymentId {} from the DB", invoiceId, paymentId);
        InvoicePayer invoice = invoicePayerRepository.findByInvoiceIdAndPaymentPaymentId(invoiceId, paymentId);
        log.debug("Finish findByInvoiceId method. DB returned [{}]", invoice);
        return invoice;
    }

    public InvoicePayer findByInvoiceIdAndPaymentIdAndRefundId(String invoiceId, String paymentId, String refundId) {
        log.debug("Trying to find InvoicePayer by invoiceId {}, paymentId {}, refundId {} from the DB", invoiceId, paymentId, refundId);
        InvoicePayer invoice = invoicePayerRepository.findByInvoiceIdAndPaymentPaymentIdAndPaymentRefundRefundId(invoiceId, paymentId, refundId);
        log.debug("Finish findByInvoiceId method. DB returned [{}]", invoice);
        return invoice;
    }

    public InvoicePayer findInvoiceByStatusRefund(String invoiceId, String paymentId, String refundId, String refundStatus) {
        log.debug("Trying to find InvoicePayer by invoiceId {}, paymentId {}, refundId {}, refundStatus {} from the DB",
                invoiceId, paymentId, refundId, refundStatus
        );
        InvoicePayer invoice = invoicePayerRepository.findByInvoiceIdAndPaymentPaymentIdAndPaymentRefundRefundIdAndPaymentRefundStatus(
                invoiceId, paymentId, refundId, refundStatus
        );
        log.debug("Finish findByInvoiceId method. DB returned [{}]", invoice);
        return invoice;
    }

    public InvoicePayer save(InvoicePayer invoicePayer) {
        log.debug("Trying to save InvoicePayer from the DB");
        InvoicePayer response = invoicePayerRepository.save(invoicePayer);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

}
