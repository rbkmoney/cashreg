package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.configuration.properties.ManagementType;
import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.handler.iface.AbstractManagementHandler;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory.createStatusChangeDelivered;

@Slf4j
@Component
public class StatusChangesDeliveredManagementHandler extends AbstractManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();

    public StatusChangesDeliveredManagementHandler(ManagementType managementType) {
        super(managementType.getStatusChangedStatusDelivered());
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        log.debug("Start {}", HANDLER_NAME);
        SourceData sourceData = SourceData.builder()
                .change(createStatusChangeDelivered())
                .complexAction(new ComplexAction())
                .build();
        log.debug("Finish {}, sourceData {}", HANDLER_NAME, sourceData);
        return sourceData;
    }

}
