package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.configuration.properties.FilterPathProperties;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.AbstractChangeMapper;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class StatusChangedChangeMapper extends AbstractChangeMapper {

    public StatusChangedChangeMapper(FilterPathProperties filterPathProperties) {
        super(filterPathProperties.getStatusChanged());
    }

    @Override
    public CashReg map(Change change) {
        return new CashReg().setStatus(change.getStatusChanged().getStatus());
    }

}
