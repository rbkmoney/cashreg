package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregator;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.MgChangeManagerMapper;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.damsel.cashreg.receipt.ReceiptNotFound;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.exception.MachineAlreadyExistsException;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.HistoryRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashRegServerManagementHandler implements ManagementSrv.Iface {

    private final AutomatonClient<Value, Change> automatonClient;
    private final ManagementAggregator managementAggregate;
    private final MgChangeManagerMapper mgChangeManagerMapper;

    @Override
    public void create(ReceiptParams receiptParams) throws ReceiptNotFound, TException {
        Change change = managementAggregate.toCashRegCreatedChange(receiptParams);
        try {
            automatonClient.start(receiptParams.getReceiptId(), ProtoUtils.toValue(Collections.singletonList(change)));
        } catch (MachineAlreadyExistsException ex) {
            log.error("Machine {} already exists", receiptParams.getReceiptId(), ex);
        }
    }

    @Override
    public Receipt get(String receiptID) throws ReceiptNotFound, TException {
        List<Change> changes = automatonClient.getEvents(receiptID).stream().map(TMachineEvent::getData).collect(Collectors.toList());
        log.info("Get receiptID {}, changes {}", receiptID, changes);
        Receipt receipt = mgChangeManagerMapper.process(changes);
        log.info("Get receiptID {}, changes {}, receipt {}", receiptID, changes, receipt);
        return receipt;
    }

    @Override
    public List<Event> getEvents(String receiptID, EventRange eventRange) throws ReceiptNotFound, TException {
        HistoryRange historyRange = new HistoryRange();
        if (eventRange.isSetAfter()) {
            historyRange.setAfter(eventRange.getAfter());
        }
        historyRange.setLimit(eventRange.getLimit());
        return automatonClient.getEvents(receiptID, historyRange).stream()
                .map(event -> new Event(
                                event.getId(),
                                event.getCreatedAt().toString(),
                                event.getData()
                        )
                ).collect(Collectors.toList());
    }

}
