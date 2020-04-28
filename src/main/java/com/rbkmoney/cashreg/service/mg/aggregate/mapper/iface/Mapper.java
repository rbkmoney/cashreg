package com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;

public interface Mapper {

    default boolean filter(Change change) {
        return getChangeType().getFilter().match(change);
    }

    Receipt map(Change change);

    ChangeType getChangeType();

}
