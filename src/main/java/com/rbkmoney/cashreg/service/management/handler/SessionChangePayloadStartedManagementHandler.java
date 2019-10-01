package com.rbkmoney.cashreg.service.management.handler;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregate;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg.provider.FinishIntent;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.damsel.domain.ProxyDefinition;
import com.rbkmoney.damsel.domain.ProxyObject;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.NETWORK_TIMEOUT;
import static com.rbkmoney.cashreg.utils.ProtoUtils.*;
import static com.rbkmoney.cashreg.utils.cashreg.creators.ChangeCreators.createSessionChange;


@Component
@RequiredArgsConstructor
public class SessionChangePayloadStartedManagementHandler implements ManagementHandler {

    private final ManagementAggregate managementAggregate;
    private final CashRegProviderService providerService;

    @Override
    public boolean filter(Change change) {
        return change.isSetSession()
                && change.getSession().isSetPayload()
                && change.getSession().getPayload().isSetStarted();
    }

    @Override
    public SourceData handle(Change change, CashReg cashReg) {

        ProxyObject proxyObject = managementAggregate.extractProxyObject(cashReg);
        ProxyDefinition proxyDefinition = proxyObject.getData();

        String url = proxyDefinition.getUrl();
        Map<String, String> proxyOptions = managementAggregate.aggregateOptions(cashReg);

        CashRegResult result =  providerService.register(url, NETWORK_TIMEOUT, prepareCashRegContext(cashReg, proxyOptions));

        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        SessionAdapterStateChanged sessionAdapterStateChanged = new SessionAdapterStateChanged();
        sessionChangePayload.setSessionAdapterStateChanged(sessionAdapterStateChanged);

        sessionAdapterStateChanged.setState(com.rbkmoney.damsel.msgpack.Value.bin(result.getState()));
        ComplexAction complexAction = null;
        if (result.getIntent().isSetSleep()) {
            sessionChangePayload.setSessionAdapterStateChanged(sessionAdapterStateChanged);

            complexAction = buildComplexActionWithTimer(
                    prepareTimer(result.getIntent().getSleep().getTimer()),
                    buildLastEventHistoryRange()
            );
        }

        if (result.getIntent().isSetFinish()) {
            FinishIntent finishIntent = result.getIntent().getFinish();
            SessionFinished sessionFinished = new SessionFinished();
            SessionResult sessionResult = new SessionResult();

            if (finishIntent.getStatus().isSetFailure()) {
                prepareSessionFailed(result, sessionFinished, sessionResult);
            } else {
                prepareSessionSucceeded(result, sessionFinished, sessionResult);
            }

            sessionChangePayload.setFinished(sessionFinished);
            complexAction = new ComplexAction();
        }

        return SourceData.builder()
                .change(createSessionChange(sessionChangePayload))
                .complexAction(complexAction)
                .build();
    }

    private void prepareSessionFailed(CashRegResult result, SessionFinished sessionFinished, SessionResult sessionResult) {
        SessionFailed sessionFailed = new SessionFailed();
        com.rbkmoney.damsel.cashreg.base.Failure failure = new com.rbkmoney.damsel.cashreg.base.Failure();
        failure.setCode(result.getIntent().getFinish().getStatus().getFailure().getCode());
        failure.setCode(result.getIntent().getFinish().getStatus().getFailure().getReason());
        sessionFailed.setFailure(failure);
        sessionResult.setFailed(sessionFailed);
        sessionFinished.setResult(sessionResult);
    }

    private void prepareSessionSucceeded(CashRegResult result, SessionFinished sessionFinished, SessionResult sessionResult) {
        SessionSucceeded sessionSucceeded = new SessionSucceeded();
        sessionSucceeded.setInfo(result.getCashregInfo());
        sessionResult.setSucceeded(sessionSucceeded);
        sessionFinished.setResult(sessionResult);
    }

}
