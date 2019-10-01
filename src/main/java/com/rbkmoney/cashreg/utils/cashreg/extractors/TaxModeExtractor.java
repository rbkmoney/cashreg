package com.rbkmoney.cashreg.utils.cashreg.extractors;

import com.rbkmoney.damsel.cashreg_domain.TaxMode;

import java.util.Map;

import static com.rbkmoney.cashreg.service.management.model.ExtraField.TAX_MODE;

public class TaxModeExtractor {

    public static TaxMode extractTaxModeFromOptions(Map<String, String> options) {
        if (!options.containsKey(TAX_MODE)) {
            throw new RuntimeException("RussianLegalEntity does not contain " + TAX_MODE);
        }
        return TaxMode.valueOf(options.get(TAX_MODE));
    }

}
