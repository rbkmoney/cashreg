package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.domain.CashRegProviderRef;

public class CashRegProviderCreators {

    public static CashRegProviderRef createCashregProviderRef(String providerRef) {
        return new CashRegProviderRef().setId(Integer.parseInt(providerRef));
    }

}
