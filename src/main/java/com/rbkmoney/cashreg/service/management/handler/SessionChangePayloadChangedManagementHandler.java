package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.converter.ManagementConverter;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SessionChangePayloadChangedManagementHandler implements ManagementHandler {

    private final CashRegProviderService providerService;
    private final ManagementConverter managementConverter;

    @Override
    public boolean filter(Change change) {
        return change.isSetSession()
                && change.getSession().isSetPayload()
                && change.getSession().getPayload().isSetSessionAdapterStateChanged();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        CashRegResult result = providerService.register(cashReg);
        return managementConverter.convert(result);
    }



}
