package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregator;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.MgChangeManagerMapper;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg.CashRegNotFound;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
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
    private final MgChangeManagerMapper mgChangeHandler;

    @Override
    public void create(CashRegParams cashRegParams) throws CashRegNotFound, TException {
        Change change = managementAggregate.toCashRegCreatedChange(cashRegParams);
        automatonClient.start(cashRegParams.getId(), ProtoUtils.toValue(Collections.singletonList(change)));
    }

    @Override
    public CashReg get(String cashRegID) throws CashRegNotFound, TException {
        List<Change> changes = automatonClient.getEvents(cashRegID, new HistoryRange()).stream().map(TMachineEvent::getData).collect(Collectors.toList());
        return mgChangeHandler.process(changes);
    }

    @Override
    public List<Event> getEvents(String cashRegID, EventRange eventRange) throws CashRegNotFound, TException {
        HistoryRange historyRange = new HistoryRange();


        if (eventRange.isSetAfter()) {
            historyRange.setAfter(eventRange.getAfter());
        }
        if (eventRange.isSetLimit()) {
            historyRange.setLimit(eventRange.getLimit());
        }

        return automatonClient.getEvents(cashRegID, historyRange).stream()
                .map(event -> new Event(
                                event.getId(),
                                event.getCreatedAt().toString(),
                                event.getData()
                        )
                ).collect(Collectors.toList());
    }

}
