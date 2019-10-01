package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinarium.domain.CallResultData;
import com.rbkmoney.machinarium.domain.SignalResultData;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.handler.AbstractProcessorHandler;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.cashreg.utils.ProtoUtils.toChangeList;
import static com.rbkmoney.cashreg.utils.ProtoUtils.toValue;


@Slf4j
@Component
public class ManagementProcessorHandler extends AbstractProcessorHandler<Value, Change> {

    private final ManagementServiceImpl managementService;

    public ManagementProcessorHandler(
            ManagementServiceImpl managementService
    ) {
        super(Value.class, Change.class);
        this.managementService = managementService;
    }

    @Override
    protected SignalResultData<Change> processSignalInit(String namespace, String machineId, Value args) {
        log.info("Request processSignalInit() machineId: {} value: {}", machineId, args);
        List<Change> changes = toChangeList(args);
        SourceData sourceData = managementService.init(changes);
        changes.add(sourceData.getChange());
        SignalResultData<Change> resultData = new SignalResultData<>(
                toChangeList(toValue(changes)),
                sourceData.getComplexAction()
        );
        log.info("Response of processSignalInit: {}", resultData);
        return resultData;
    }

    @Override
    protected SignalResultData<Change> processSignalTimeout(String namespace, String machineId, List<TMachineEvent<Change>> tMachineEvents) {
        log.info("Request processSignalTimeout() machineId: {} list: {}", machineId, tMachineEvents);
        List<Change> changes = tMachineEvents.stream().map(TMachineEvent::getData).collect(Collectors.toList());
        SourceData sourceData = managementService.timeout(changes);
        changes.add(sourceData.getChange());
        SignalResultData<Change> resultData = new SignalResultData<>(
                toChangeList(toValue(changes)),
                sourceData.getComplexAction()
        );
        log.info("Response of processSignalTimeout: {}", resultData);
        return resultData;
    }

    @Override
    protected CallResultData<Change> processCall(String namespace, String machineId, Value args, List<TMachineEvent<Change>> tMachineEvents) {
        return new CallResultData<>(getLastEvent(tMachineEvents), Collections.emptyList(), new ComplexAction());
    }

    private Change getLastEvent(List<TMachineEvent<Change>> tMachineEvents) {
        if (tMachineEvents.isEmpty()) {
            return null;
        }
        return tMachineEvents.get(tMachineEvents.size() - 1).getData();
    }

}
