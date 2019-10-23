package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChangesFailedManagementHandler implements ManagementHandler {

    private final String HANDLER_NAME = this.getClass().getSimpleName();

    @Override
    public boolean filter(Change change) {
        return change.isSetStatusChanged()
                && change.getStatusChanged().getStatus().isSetFailed();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        log.info("Start {}", HANDLER_NAME);
        SourceData sourceData = SourceData.builder()
                .change(ChangeFactory.createStatusChangeFailed())
                .complexAction(new ComplexAction())
                .build();
        log.info("Finish {}, sourceData {}", HANDLER_NAME, sourceData);
        return sourceData;
    }
}
