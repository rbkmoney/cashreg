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
public class SessionChangePayloadFinishedSucceededManagementHandler implements ManagementHandler {

    @Override
    public boolean filter(Change change) {
        return change.isSetSession()
                && change.getSession().isSetPayload()
                && change.getSession().getPayload().isSetFinished()
                && change.getSession().getPayload().getFinished().isSetResult()
                && change.getSession().getPayload().getFinished().getResult().isSetSucceeded();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {
        return SourceData.builder()
                .change(ChangeFactory.createStatusChangeDelivered())
                .complexAction(new ComplexAction())
                .build();
    }

}
