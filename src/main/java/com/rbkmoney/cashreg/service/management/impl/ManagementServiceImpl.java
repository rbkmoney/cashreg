package com.rbkmoney.cashreg.service.management.impl;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.ManagementService;
import com.rbkmoney.cashreg.service.management.handler.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.handler.MgChangeHandler;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.base.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildLastEventHistoryRange;
import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeCreators.createStatusChangePending;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {

    public static final int DEFAULT_TIMER = 1;
    public static final int NETWORK_TIMEOUT = 10;

    private final MgChangeHandler mgChangeHandler;
    private final List<ManagementHandler> managementHandlers;

    @Override
    public SourceData init(List<Change> changes) {
        return SourceData.builder()
                .change(createStatusChangePending())
                .complexAction(
                        buildComplexActionWithTimer(
                                Timer.timeout(DEFAULT_TIMER),
                                buildLastEventHistoryRange()
                        )
                ).build();
    }

    @Override
    public SourceData timeout(List<Change> changes) {
        Change lastChange = getLastChange(changes);
        if (lastChange == null) {
            throw new IllegalArgumentException("Don't have events");
        }

        CashReg cashReg = mgChangeHandler.listChangesToCashReg(changes);
        return managementHandlers.stream()
                .filter(handler -> handler.filter(lastChange))
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .handle(lastChange, cashReg);
    }

    private Change getLastChange(List<Change> changes) {
        return changes.get(changes.size() - 1);
    }
}
