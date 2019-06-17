package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.InvoicePayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InvoicePayerRepository extends JpaRepository<InvoicePayer, Long> {

    InvoicePayer findByInvoiceId(String invoiceId);

    InvoicePayer findByInvoiceIdAndPaymentPaymentId(String invoiceId, String paymentId);

    InvoicePayer findByInvoiceIdAndPaymentPaymentIdAndPaymentRefundRefundId(String invoiceId, String paymentId, String refundId);

    InvoicePayer findByInvoiceIdAndPaymentPaymentIdAndPaymentRefundRefundIdAndPaymentRefundStatus(
            String invoiceId, String paymentId, String refundId, String status
    );

}
