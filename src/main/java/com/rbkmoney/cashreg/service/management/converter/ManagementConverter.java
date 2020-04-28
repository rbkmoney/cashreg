package com.rbkmoney.cashreg.service.management.converter;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.cashreg.adapter.FinishIntent;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.rbkmoney.cashreg.utils.ProtoUtils.*;
import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory.createSessionChange;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementConverter implements Converter<CashregResult, SourceData> {

    @Override
    public SourceData convert(CashregResult result) {
        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        SessionAdapterStateChanged sessionAdapterStateChanged = new SessionAdapterStateChanged();
        sessionChangePayload.setSessionAdapterStateChanged(sessionAdapterStateChanged);

        if (result.getState() != null) {
            sessionAdapterStateChanged.setState(com.rbkmoney.damsel.msgpack.Value.bin(result.getState()));
        }

        ComplexAction complexAction = new ComplexAction();
        if (result.getIntent().isSetSleep()) {
            sessionChangePayload.setSessionAdapterStateChanged(sessionAdapterStateChanged);
            complexAction = buildComplexActionWithTimer(
                    prepareTimer(result.getIntent().getSleep().getTimer()),
                    buildLastEventHistoryRange()
            );
        }

        if (result.getIntent().isSetFinish()) {
            sessionChangePayload.setFinished(prepareSessionFinished(result));
        }

        String sessionId = UUID.randomUUID().toString();
        return SourceData.builder()
                .change(createSessionChange(sessionId, sessionChangePayload))
                .complexAction(complexAction)
                .build();
    }

    private SessionFinished prepareSessionFinished(CashregResult result) {
        FinishIntent finishIntent = result.getIntent().getFinish();
        SessionFinished sessionFinished = new SessionFinished();
        SessionResult sessionResult = new SessionResult();

        if (finishIntent.getStatus().isSetFailure()) {
            prepareSessionFailed(result, sessionFinished, sessionResult);
        } else {
            prepareSessionSucceeded(result, sessionFinished, sessionResult);
        }
        return sessionFinished;
    }

    private void prepareSessionFailed(CashregResult result, SessionFinished sessionFinished, SessionResult sessionResult) {
        com.rbkmoney.damsel.domain.Failure failure = result.getIntent().getFinish().getStatus().getFailure();
        sessionResult.setFailed(new SessionFailed().setFailure(
                new com.rbkmoney.damsel.cashreg.base.Failure()
                        .setCode(failure.getCode())
                        .setReason(failure.getReason())
                )
        );
        sessionFinished.setResult(sessionResult);
    }

    private void prepareSessionSucceeded(CashregResult result, SessionFinished sessionFinished, SessionResult sessionResult) {
        sessionResult.setSucceeded(
                new SessionSucceeded()
                        .setInfo(result.getInfo())
        );
        sessionFinished.setResult(sessionResult);
    }
}


