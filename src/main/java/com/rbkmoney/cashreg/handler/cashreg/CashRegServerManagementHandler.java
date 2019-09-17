package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg.CashRegNotFound;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.cashreg.utils.ProtoUtils.toCashReg;
import static com.rbkmoney.cashreg.utils.ProtoUtils.toCashRegCreated;

@Slf4j
@Component
public class CashRegServerManagementHandler implements ManagementSrv.Iface {

    private final AutomatonClient<Value, Change> automatonClient;

    public CashRegServerManagementHandler(
            AutomatonClient<Value, Change> automatonClient
    ) {
        this.automatonClient = automatonClient;
    }

    // Need do void
    @Override
    public CashReg create(CashRegParams cashRegParams) throws CashRegNotFound, TException {
        Change change = toCashRegCreated(cashRegParams);
        automatonClient.start(cashRegParams.getId(), ProtoUtils.toValue(Collections.singletonList(change)));
        return null;
    }

    @Override
    public CashReg get(String cashRegID) throws CashRegNotFound, TException {
        Change change = automatonClient.getEvents(cashRegID).stream().findFirst().map(TMachineEvent::getData).orElse(null);
        return toCashReg(change);
    }

    @Override
    public List<Event> getEvents(String cashRegID, EventRange eventRange) throws CashRegNotFound, TException {
        // TODO: what about event range?
        // TODO: Event Sink?
        return automatonClient.getMachine(cashRegID)
                .getHistory().stream()
                .map(event -> new Event(
                                event.getId(),
                                event.getCreatedAt(),
                                ProtoUtils.toChangeList(event.getData()).get(0)
                        )
                ).collect(Collectors.toList());
    }

    private Change getLastChange(List<TMachineEvent<Change>> machineEvents) {
        return machineEvents.get(machineEvents.size() - 1).getData();
    }

}
