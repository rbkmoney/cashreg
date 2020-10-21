package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.machinegun.base.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.DEFAULT_TIMER_SEC;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;

@Slf4j
@Component
public class SessionChangePayloadFinishedFailedManagementHandler implements ManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        log.info("Start {} change {}, receipt {}", HANDLER_NAME, change, receipt);
        SourceData sourceData = SourceData.builder()
                .change(ChangeFactory.createStatusChangeFailed())
                .complexAction(buildComplexActionWithTimer(
                        Timer.timeout(DEFAULT_TIMER_SEC),
                        ProtoUtils.buildDirectionBackwardEventHistoryRange()
                        )
                )
                .build();
        log.info("Finish {} change {}, receipt {}, sourceData {}", HANDLER_NAME, change, receipt, sourceData);
        return sourceData;
    }


    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_PAYLOAD_FINISHED_RESULT_FAILED;
    }

}
