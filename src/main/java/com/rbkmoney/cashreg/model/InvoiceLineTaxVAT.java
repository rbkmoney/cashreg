package com.rbkmoney.cashreg.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceLineTaxVAT {

    VAT_0("0%", "0% НДС"),
    VAT_10("10%", "10% НДС"),
    VAT_18("18%", "18% НДС"),
    NO_VAT("null", "Без НДС"),
    VAT_18_118("10/110", "10/110 НДС"),
    VAT_10_110("18/118", "18/118 НДС");

    private final String code;
    private final String message;

    public static InvoiceLineTaxVAT valueStringOf(String vatCode) {
        for (InvoiceLineTaxVAT vat : values()) {
            if (vat.code.equals(vatCode)) {
                return vat;
            }
        }
        throw new IllegalArgumentException("No matching for [" + vatCode + "]");
    }

}
