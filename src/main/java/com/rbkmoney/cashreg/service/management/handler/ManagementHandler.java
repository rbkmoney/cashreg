package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;

public interface ManagementHandler {

    boolean filter(Change change);

    SourceData handle(Change change, CashReg cashReg);

}
