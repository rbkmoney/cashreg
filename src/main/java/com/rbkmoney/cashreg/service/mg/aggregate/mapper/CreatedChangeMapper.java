package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class CreatedChangeMapper implements Mapper {

    @Override
    public CashReg map(Change change) {
        return change.getCreated().getCashreg();
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.CREATED;
    }

}
