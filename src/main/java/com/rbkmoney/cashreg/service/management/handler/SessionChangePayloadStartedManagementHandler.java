package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.configuration.properties.FilterPathProperties;
import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.converter.ManagementConverter;
import com.rbkmoney.cashreg.service.management.handler.iface.AbstractManagementHandler;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionChangePayloadStartedManagementHandler extends AbstractManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();
    private final CashRegProviderService providerService;
    private final ManagementConverter managementConverter;

    public SessionChangePayloadStartedManagementHandler(
            FilterPathProperties filterPathProperties,
            CashRegProviderService providerService,
            ManagementConverter managementConverter
    ) {
        super(filterPathProperties.getSessionPayloadStarted());
        this.providerService = providerService;
        this.managementConverter = managementConverter;
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        log.debug("Start {}", HANDLER_NAME);
        CashRegResult result = providerService.register(cashReg);
        log.debug("Finish {}, result {}", HANDLER_NAME, result);
        return managementConverter.convert(result);
    }

}
