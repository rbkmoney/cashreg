package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.status.Delivered;
import com.rbkmoney.damsel.cashreg.status.Failed;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.SessionFinished;
import com.rbkmoney.damsel.cashreg_processing.SessionResult;
import org.springframework.stereotype.Component;

@Component
public class SessionFinishedChangeMapper implements Mapper {

    @Override
    public CashReg map(Change change) {
        SessionFinished sessionFinished = change.getSession().getPayload().getFinished();
        SessionResult sessionResult = sessionFinished.getResult();
        CashReg cashReg = new CashReg();

        if (sessionResult.isSetFailed()) {
            cashReg.setStatus(Status.failed(new Failed()));
        } else {
            cashReg.setInfo(sessionResult.getSucceeded().getInfo());
            cashReg.setStatus(Status.delivered(new Delivered()));
        }
        return cashReg;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_PAYLOAD_FINISHED;
    }
}
