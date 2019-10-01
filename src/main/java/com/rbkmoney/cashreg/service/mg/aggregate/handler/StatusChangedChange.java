package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class StatusChangedChange implements ChangeHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetStatusChanged();
    }

    @Override
    public CashReg handle(Change change) {
        return new CashReg().setStatus(change.getStatusChanged().getStatus());
    }

}
