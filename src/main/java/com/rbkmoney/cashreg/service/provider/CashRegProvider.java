package com.rbkmoney.cashreg.service.provider;

import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg_processing.CashReg;

public interface CashRegProvider {
    CashRegResult register(CashReg cashReg);
}
