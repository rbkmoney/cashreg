package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.configuration.properties.ManagementType;
import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.AbstractManagementHandler;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.base.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.DEFAULT_TIMER_SEC;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildLastEventHistoryRange;

@Slf4j
@Component
public class StatusChangesPendingManagementHandler extends AbstractManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();

    public StatusChangesPendingManagementHandler(ManagementType managementType) {
        super(managementType.getStatusChangedStatusPending());
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        log.debug("Start {}", HANDLER_NAME);
        SourceData sourceData = SourceData.builder()
                .change(ChangeFactory.createSessionChangeStarted())
                .complexAction(
                        buildComplexActionWithTimer(
                                Timer.timeout(DEFAULT_TIMER_SEC),
                                buildLastEventHistoryRange())
                )
                .build();
        log.debug("Finish {}, sourceData {}", HANDLER_NAME, sourceData);
        return sourceData;
    }

}
