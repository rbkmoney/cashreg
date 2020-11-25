package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.cashreg.utils.cashreg.extractors.ReceiptExtractor;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatusChangesFailedManagementHandler implements ManagementHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        String receiptId = ReceiptExtractor.extractReceiptId(receipt);
        log.info("Start {} receiptId {}", handlerEvent, receiptId);
        SourceData sourceData = SourceData.builder()
                .change(ChangeFactory.createStatusChangeFailed())
                .complexAction(new ComplexAction())
                .build();
        log.info("Finish {} receiptId {}, sourceData {}", handlerEvent, receiptId, sourceData);
        return sourceData;
    }


    @Override
    public ChangeType getChangeType() {
        return ChangeType.STATUS_CHANGED_STATUS_FAILED;
    }
}
