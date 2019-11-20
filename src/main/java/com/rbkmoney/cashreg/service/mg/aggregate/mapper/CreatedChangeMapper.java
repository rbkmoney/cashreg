package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.configuration.properties.FilterPathProperties;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.AbstractChangeMapper;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import org.springframework.stereotype.Component;

@Component
public class CreatedChangeMapper extends AbstractChangeMapper {

    public CreatedChangeMapper(FilterPathProperties filterPathProperties) {
        super(filterPathProperties.getCreated());
    }

    @Override
    public CashReg map(Change change) {
        return change.getCreated().getCashreg();
    }

}
