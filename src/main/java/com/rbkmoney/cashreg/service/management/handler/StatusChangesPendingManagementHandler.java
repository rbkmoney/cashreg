package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.base.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.DEFAULT_TIMER_SEC;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildLastEventHistoryRange;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChangesPendingManagementHandler implements ManagementHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetStatusChanged()
                && change.getStatusChanged().getStatus().isSetPending();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        return SourceData.builder()
                .change(ChangeFactory.createSessionChangeStarted())
                .complexAction(
                        buildComplexActionWithTimer(
                                Timer.timeout(DEFAULT_TIMER_SEC),
                                buildLastEventHistoryRange())
                )
                .build();
    }

}
