package com.rbkmoney.cashreg.service.provider;

import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;

public interface CashRegProvider {
    CashregResult register(Receipt receipt, Change change);
}
