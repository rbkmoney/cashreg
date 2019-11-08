package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.configuration.properties.ManagementType;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.AbstractChangeMapper;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class SessionStartedChangeMapper extends AbstractChangeMapper {

    public SessionStartedChangeMapper(ManagementType managementType) {
        super(managementType.getSessionPayloadStarted());
    }

    @Override
    public CashReg map(Change change) {
        return new CashReg().setStatus(Status.pending(new Pending()));
    }

}
