package com.rbkmoney.cashreg.utils.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.entity.Refund;
import com.rbkmoney.damsel.domain.InvoiceLine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.cashreg.utils.constant.Tax.TAX_MODE;

public class CartUtils {

    public static String prepareCartInvoiceLine(ObjectMapper objectMapper, List<InvoiceLine> lineList) throws JsonProcessingException {
        List<com.rbkmoney.cashreg.model.InvoiceLine> invoiceLines = new ArrayList<>();

        lineList.forEach(item -> {
            com.rbkmoney.cashreg.model.InvoiceLine invoiceLine = new com.rbkmoney.cashreg.model.InvoiceLine();

            invoiceLine.setProduct(prepareProduct(item.getProduct()));
            invoiceLine.setPrice(BigInteger.valueOf(item.getPrice().getAmount()));
            invoiceLine.setQuantity(item.getQuantity());
            Map<String, String> metadata = new HashMap<>();
            if (item.getMetadata().get(TAX_MODE) != null) {
                metadata.put(TAX_MODE, item.getMetadata().get(TAX_MODE).getStr());
            } else {
                metadata.put(TAX_MODE, null);
            }
            invoiceLine.setMetadata(metadata);
            invoiceLines.add(invoiceLine);
        });

        return objectMapper.writeValueAsString(invoiceLines);
    }

    private static String prepareProduct(String product) {
        return product.replaceAll("[\\\\\\\"\':\\/]", "");
    }

    public static CashRegDelivery prepareCashRegDelivery(InvoicePayer invoicePayer, Payment paymentDB, Refund refundId, String typeOperation, String cashRegStatus, String cartState) {
        return CashRegDelivery.builder()
                .invoiceId(invoicePayer)
                .paymentId(paymentDB)
                .refundId(refundId)
                .typeOperation(typeOperation)
                .cashregStatus(cashRegStatus)
                .cartState(cartState).build();
    }

}
