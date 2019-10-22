package com.rbkmoney.cashreg.utils;

import com.rbkmoney.cashreg.service.management.model.ExtraField;

import java.util.HashMap;
import java.util.Map;

public class TestData {

    public static final String CASHREG_RECEIPT_ID = "cashreg_receipt_id";

    public static final String CASHREG_NAMESPACE = "cashreg";
    public static final String CASHREG_CASHREG_PROVIDER_ID = "1905";
    public static final String CASHREG_ID = "cashreg_id";
    public static final String PARTY_ID = "party_id";
    public static final String SHOP_ID = "shop_id";
    public static final String TEST_EMAIL = "test@test.ru";

    public static final String PROVIDER_NAME = "providerName";
    public static final String PROVIDER_DESCRIPTION = "providerDescription";

    public static final String PROXY_NAME = "proxyName";
    public static final String PROXY_DESCRIPTION = "proxyDescription";
    public static final String PROXY_URL = "http://localhost";

    public static final String TERMINAL_NAME = "terminalName";
    public static final String TERMINAL_DESCRIPTION = "terminalDescription";

    public static final String SHOP_CONTRACT_ID = "shop_contract_id";

    public static Map<String, String> prepareOptions() {
        Map<String, String> options = new HashMap<>();
        options.put(ExtraField.TAX_MODE.getField(), "osn");
        options.put(ExtraField.RUSSIAN_LEGAL_ENTITY_EMAIL.getField(), TestData.TEST_EMAIL);
        return options;
    }

}
