package com.rbkmoney.cashreg.service.management.impl;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.ManagementService;
import com.rbkmoney.cashreg.service.management.handler.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.MgChangeManagerMapper;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.base.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildLastEventHistoryRange;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {

    public static final int DEFAULT_TIMER_SEC = 1;
    public static final int NETWORK_TIMEOUT_SEC = 10;

    private final MgChangeManagerMapper mgChangeManagerMapper;
    private final List<ManagementHandler> managementHandlers;

    @Override
    public SourceData signalInit() {
        return SourceData.builder()
                .change(ChangeFactory.createStatusChangePending())
                .complexAction(
                        buildComplexActionWithTimer(
                                Timer.timeout(DEFAULT_TIMER_SEC),
                                buildLastEventHistoryRange()
                        )
                ).build();
    }

    @Override
    public SourceData signalTimeout(List<Change> changes) {
        Change lastChange = getLastChange(changes);
        CashReg cashReg = mgChangeManagerMapper.process(changes);
        return managementHandlers.stream()
                .filter(handler -> handler.filter(lastChange))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't found handler"))
                .handle(lastChange, cashReg);
    }

    private Change getLastChange(List<Change> changes) {
        return changes.get(changes.size() - 1);
    }
}
