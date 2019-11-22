package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class StatusChangedChangeMapper implements Mapper {

    @Override
    public CashReg map(Change change) {
        return new CashReg().setStatus(change.getStatusChanged().getStatus());
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.STATUS_CHANGED;
    }

}
