package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import org.springframework.stereotype.Component;

@Component
public class SessionAdapterStateChangedChangeMapper implements Mapper {

    @Override
    public Receipt map(Change change) {
        return new Receipt().setStatus(Status.pending(new Pending()));
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_ADAPTER_STATE_CHANGED;
    }
}
