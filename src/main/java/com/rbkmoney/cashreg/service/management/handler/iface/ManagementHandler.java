package com.rbkmoney.cashreg.service.management.handler.iface;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;

public interface ManagementHandler {

    default boolean filter(Change change, Receipt receipt) {
        return getChangeType().getFilter().match(change);
    }

    SourceData handle(Change change, Receipt receipt);

    ChangeType getChangeType();

}
