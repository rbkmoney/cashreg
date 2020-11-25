package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.converter.ManagementConverter;
import com.rbkmoney.cashreg.service.management.converter.StateResultWrapper;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.cashreg.utils.cashreg.extractors.ReceiptExtractor;
import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.damsel.msgpack.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionChangePayloadChangedManagementHandler implements ManagementHandler {

    private String handlerEvent = this.getClass().getSimpleName();
    private final CashRegProviderService providerService;
    private final ManagementConverter managementConverter;

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        String receiptId = ReceiptExtractor.extractReceiptId(receipt);
        log.info("Start {} receiptId {}", handlerEvent, receiptId);
        Value value = change.getSession().getPayload().getSessionAdapterStateChanged().getState();
        CashregResult result = providerService.register(receipt, value);
        log.info("Finish {} receiptId {}, result {}", handlerEvent, receiptId, result);
        StateResultWrapper wrapper = StateResultWrapper.builder().value(value).result(result).build();
        return managementConverter.convert(wrapper);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_ADAPTER_STATE_CHANGED;
    }

}
