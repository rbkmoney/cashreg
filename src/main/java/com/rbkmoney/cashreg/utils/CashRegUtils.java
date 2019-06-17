package com.rbkmoney.cashreg.utils;


import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.entity.Refund;

public class CashRegUtils {

    public static String prepareUuid(InvoicePayer invoicePayer, Payment payment, String type) {
        return String.format("%s-%s-%s", invoicePayer.getInvoiceId(), payment.getPaymentId(), type);
    }

    public static String prepareUuid(InvoicePayer invoicePayer, Payment payment, Refund refund, String type) {

        if(refund == null) {
            return prepareUuid(invoicePayer, payment, type);
        }

        return String.format("%s-%s-%s-%s", invoicePayer.getInvoiceId(), payment.getPaymentId(), refund.getRefundId(), type);
    }

}
