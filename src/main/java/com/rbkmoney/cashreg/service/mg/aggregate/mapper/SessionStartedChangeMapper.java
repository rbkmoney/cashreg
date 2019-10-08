package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class SessionStartedChangeMapper implements ChangeMapper {

    @Override
    public boolean filter(Change change) {
        return change.isSetSession()
                && change.getSession().isSetPayload()
                && change.getSession().getPayload().isSetStarted();
    }

    @Override
    public CashReg map(Change change) {
        return new CashReg().setStatus(Status.pending(new Pending()));
    }

}
