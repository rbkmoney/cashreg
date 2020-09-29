package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.ManagementHandler;
import com.rbkmoney.cashreg.service.mg.aggregate.mapper.ChangeType;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory.createStatusChangeDelivered;

@Slf4j
@Component
public class StatusChangesDeliveredManagementHandler implements ManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();

    @Override
    public SourceData handle(Change change, Receipt receipt) {
        log.info("Start {}, change {}, receipt {}", HANDLER_NAME, change, receipt);
        SourceData sourceData = SourceData.builder()
                .change(createStatusChangeDelivered())
                .complexAction(new ComplexAction())
                .build();
        log.info("Finish {} change {}, receipt {}, sourceData {}", HANDLER_NAME, change, receipt, sourceData);
        return sourceData;
    }


    @Override
    public ChangeType getChangeType() {
        return ChangeType.STATUS_CHANGED_STATUS_DELIVERED;
    }

}
