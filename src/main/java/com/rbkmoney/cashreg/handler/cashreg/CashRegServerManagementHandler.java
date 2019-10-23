package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregator;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.MgChangeManagerMapper;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.HistoryRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void create(CashRegParams cashRegParams) {
        Change change = managementAggregate.toCashRegCreatedChange(cashRegParams);
        automatonClient.start(cashRegParams.getCashregId(), ProtoUtils.toValue(Collections.singletonList(change)));
    }

    @Override
    public CashReg get(String cashRegID) {
        List<Change> changes = automatonClient.getEvents(cashRegID).stream().map(TMachineEvent::getData).collect(Collectors.toList());
        return mgChangeManagerMapper.process(changes);
    }

    @Override
    public List<Event> getEvents(String cashRegID, EventRange eventRange) {
        HistoryRange historyRange = new HistoryRange();
        if (eventRange.isSetAfter()) {
            historyRange.setAfter(eventRange.getAfter());
        }
        historyRange.setLimit(eventRange.getLimit());
        return automatonClient.getEvents(cashRegID, historyRange).stream()
                .map(event -> new Event(
                                event.getId(),
                                event.getCreatedAt().toString(),
                                event.getData()
                        )
                ).collect(Collectors.toList());
    }

}
