package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.SessionFinished;
import com.rbkmoney.damsel.cashreg_processing.SessionResult;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators.createDeliveredStatus;
import static com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators.createFailedStatus;

@Component
public class SessionFinishedChange implements ChangeHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetSession() && change.getSession().getPayload().isSetFinished();
    }

    @Override
    public CashReg handle(Change change) {
        SessionFinished sessionFinished = change.getSession().getPayload().getFinished();
        SessionResult sessionResult = sessionFinished.getResult();
        CashReg cashReg = new CashReg();
        if (sessionResult.isSetFailed()) {
            cashReg.setStatus(createFailedStatus());
        }
        if (sessionResult.isSetSucceeded()) {
            cashReg.setInfo(sessionResult.getSucceeded().getInfo());
            cashReg.setStatus(createDeliveredStatus());
        }
        return cashReg;
    }
}
