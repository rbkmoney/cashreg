package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;

public interface ChangeHandler {

    boolean filter(Change change);

    CashReg handle(Change change);

}
