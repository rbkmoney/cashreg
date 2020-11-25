package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.cashreg.utils.cashreg.extractors.ReceiptExtractor;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.machinegun.base.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.DEFAULT_TIMER_SEC;
import static com.rbkmoney.cashreg.utils.ProtoUtils.buildComplexActionWithTimer;

@Slf4j
@Component
public class SessionChangePayloadFinishedSucceededManagementHandler implements ManagementHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        String receiptId = ReceiptExtractor.extractReceiptId(receipt);
        log.info("Start {} receiptId {}", handlerEvent, receiptId);
        SourceData sourceData = SourceData.builder()
                .change(ChangeFactory.createStatusChangeDelivered())
                .complexAction(buildComplexActionWithTimer(
                        Timer.timeout(DEFAULT_TIMER_SEC),
                        ProtoUtils.buildDirectionBackwardEventHistoryRange()
                        )
                )
                .build();
        log.info("Finish {} receiptId {}, sourceData {}", handlerEvent, receiptId, sourceData);
        return sourceData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_PAYLOAD_FINISHED_RESULT_SUCCEEDED;
    }

}
