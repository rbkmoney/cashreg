package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class SessionAdapterStateChangedChange implements ChangeHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetSession() && change.getSession().getPayload().isSetSessionAdapterStateChanged();
    }

    @Override
    public CashReg handle(Change change) {
        return new CashReg().setStatus(StatusCreators.createPendingStatus());
    }
}
