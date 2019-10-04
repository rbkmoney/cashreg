package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;

public interface ChangeMapper {

    boolean filter(Change change);

    CashReg map(Change change);

}
