package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class CreatedChange implements ChangeHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetCreated();
    }

    @Override
    public CashReg handle(Change change) {
        return change.getCreated().getCashreg();
    }

}
