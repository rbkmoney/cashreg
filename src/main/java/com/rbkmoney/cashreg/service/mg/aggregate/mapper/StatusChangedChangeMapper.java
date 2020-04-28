package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import org.springframework.stereotype.Component;

@Component
public class StatusChangedChangeMapper implements Mapper {

    @Override
    public Receipt map(Change change) {
        return new Receipt().setStatus(change.getStatusChanged().getStatus());
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.STATUS_CHANGED;
    }

}
