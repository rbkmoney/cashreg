package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class CreatedChangeMapper implements ChangeMapper {

    @Override
    public boolean filter(Change change) {
        return change.isSetCreated();
    }

    @Override
    public CashReg map(Change change) {
        return change.getCreated().getCashreg();
    }

}
