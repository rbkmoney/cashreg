package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.converter.ManagementConverter;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionChangePayloadStartedManagementHandler implements ManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();
    private final CashRegProviderService providerService;
    private final ManagementConverter managementConverter;

    @Override
    public boolean filter(Change change) {
        return change.isSetSession()
                && change.getSession().isSetPayload()
                && change.getSession().getPayload().isSetStarted();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        log.debug("Start {}", HANDLER_NAME);
        CashRegResult result = providerService.register(cashReg);
        log.debug("Finish {}, result {}", HANDLER_NAME, result);
        return managementConverter.convert(result);
    }

}
