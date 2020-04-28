package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.damsel.cashreg.processing.SessionFinished;
import com.rbkmoney.damsel.cashreg.processing.SessionResult;
import com.rbkmoney.damsel.cashreg.receipt.status.Delivered;
import com.rbkmoney.damsel.cashreg.receipt.status.Failed;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import org.springframework.stereotype.Component;

@Component
public class SessionFinishedChangeMapper implements Mapper {

    @Override
    public Receipt map(Change change) {
        SessionFinished sessionFinished = change.getSession().getPayload().getFinished();
        SessionResult sessionResult = sessionFinished.getResult();
        Receipt receipt = new Receipt();

        if (sessionResult.isSetFailed()) {
            receipt.setStatus(Status.failed(new Failed()));
        } else {
            receipt.setInfo(sessionResult.getSucceeded().getInfo());
            receipt.setStatus(Status.delivered(new Delivered()));
        }
        return receipt;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.SESSION_PAYLOAD_FINISHED;
    }
}
