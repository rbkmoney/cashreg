package com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;

public interface Mapper {

    default boolean filter(Change change) {
        return getChangeType().getFilter().match(change);
    }

    CashReg map(Change change);

    ChangeType getChangeType();

}
