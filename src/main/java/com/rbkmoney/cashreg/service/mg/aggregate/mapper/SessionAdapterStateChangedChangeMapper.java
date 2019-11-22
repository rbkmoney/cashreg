package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class SessionAdapterStateChangedChangeMapper implements Mapper {

    @Override
    public CashReg map(Change change) {
        return new CashReg().setStatus(Status.pending(new Pending()));
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_ADAPTER_STATE_CHANGED;
    }
}
