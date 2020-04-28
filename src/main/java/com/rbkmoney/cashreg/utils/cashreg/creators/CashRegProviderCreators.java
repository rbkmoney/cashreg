package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.domain.CashRegisterProviderRef;

public class CashRegProviderCreators {

    public static CashRegisterProviderRef createCashregProviderRef(String providerRef) {
        return new CashRegisterProviderRef().setId(Integer.parseInt(providerRef));
    }

}
