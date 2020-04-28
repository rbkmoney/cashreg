package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import org.springframework.stereotype.Component;

@Component
public class CreatedChangeMapper implements Mapper {

    @Override
    public Receipt map(Change change) {
        return change.getCreated().getReceipt();
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.CREATED;
    }

}
