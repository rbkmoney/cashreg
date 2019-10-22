package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory.createStatusChangeDelivered;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChangesDeliveredManagementHandler implements ManagementHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetStatusChanged()
                && change.getStatusChanged().getStatus().isSetFailed();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        return SourceData.builder()
                .change(createStatusChangeDelivered())
                .complexAction(new ComplexAction())
                .build();
    }

}
