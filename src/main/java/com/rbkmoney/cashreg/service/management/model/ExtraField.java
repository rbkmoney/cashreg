package com.rbkmoney.cashreg.service.management.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExtraField {

    TAX_MODE("tax_mode"),
    COMPANY_EMAIL("company_email");

    @Getter
    private final String field;

}
