package com.rbkmoney.cashreg.utils.cashreg.extractors;

import com.rbkmoney.damsel.cashreg.processing.Receipt;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReceiptExtractor {

    public static String extractReceiptId(Receipt receipt) {
        if(receipt != null && receipt.getReceiptId() != null) {
            return receipt.getReceiptId();
        }
        return StringUtils.EMPTY;
    }

}
