package com.rbkmoney.cashreg.utils.cashreg.extractors;

import com.rbkmoney.cashreg.service.management.model.ExtraField;
import com.rbkmoney.damsel.cashreg_domain.TaxMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.rbkmoney.cashreg.service.management.model.ExtraField.TAX_MODE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxModeExtractor {

    public static TaxMode extractTaxModeFromOptions(Map<String, String> options) {
        if (!options.containsKey(ExtraField.TAX_MODE.getField())) {
            throw new RuntimeException("RussianLegalEntity does not contain " + TAX_MODE);
        }
        return TaxMode.valueOf(options.get(ExtraField.TAX_MODE.getField()));
    }

}
