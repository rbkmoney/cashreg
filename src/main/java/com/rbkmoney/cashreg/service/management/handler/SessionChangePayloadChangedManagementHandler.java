package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.converter.ManagementConverter;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionChangePayloadChangedManagementHandler implements ManagementHandler {

    private String HANDLER_NAME = this.getClass().getSimpleName();
    private final CashRegProviderService providerService;
    private final ManagementConverter managementConverter;

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        log.info("Start {} change {}, receipt {}", HANDLER_NAME, change, receipt);
        CashregResult result = providerService.register(receipt, change);
        log.info("Finish {} change {}, receipt {}, result {}", HANDLER_NAME, change, receipt, result);
        return managementConverter.convert(result);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_ADAPTER_STATE_CHANGED;
    }

}
