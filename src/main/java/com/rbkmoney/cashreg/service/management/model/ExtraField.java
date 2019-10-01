package com.rbkmoney.cashreg.service.management.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExtraField {

    TAX_MODE("tax_mode"),
    RUSSIAN_LEGAL_ENTITY_EMAIL("russian_legal_entity_email");

    @Getter
    private final String field;

}
