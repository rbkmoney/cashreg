package com.rbkmoney.cashreg.service.management.impl;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.ManagementService;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.MgChangeManagerMapper;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
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
        log.info("signalTimeout start changes {}", changes);
        Change lastChange = getLastChange(changes);
        log.info("signalTimeout changes {}, lastChanges {}", changes, lastChange);
        Receipt receipt = mgChangeManagerMapper.process(changes);
        log.info("signalTimeout changes {}, lastChanges {}, receipt {}", changes, lastChange, receipt);
        return managementHandlers.stream()
                .filter(handler -> handler.filter(lastChange, receipt))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't found handler"))
                .handle(lastChange, receipt);
    }

    private Change getLastChange(List<Change> changes) {
        return changes.get(changes.size() - 1);
    }
}
