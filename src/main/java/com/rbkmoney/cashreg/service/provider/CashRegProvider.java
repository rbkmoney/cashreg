package com.rbkmoney.cashreg.service.provider;

import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;

public interface CashRegProvider {
    CashRegResult register(String url, Integer timeout, CashRegContext context);
}
