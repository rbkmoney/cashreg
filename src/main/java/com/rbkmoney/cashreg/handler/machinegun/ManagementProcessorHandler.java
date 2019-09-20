package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.CashRegProviderSrv;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg.provider.FinishIntent;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.damsel.domain.ProxyDefinition;
import com.rbkmoney.damsel.domain.ProxyObject;
import com.rbkmoney.machinarium.domain.CallResultData;
import com.rbkmoney.machinarium.domain.SignalResultData;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.handler.AbstractProcessorHandler;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rbkmoney.cashreg.utils.ProtoUtils.*;


@Slf4j
@Component
public class ManagementProcessorHandler extends AbstractProcessorHandler<Value, Change> {

    private final PartyManagementService partyManagementService;
    private final DominantService dominantService;

    public ManagementProcessorHandler(
            PartyManagementService partyManagementService,
            DominantService dominantService
    ) {
        super(Value.class, Change.class);
        this.partyManagementService = partyManagementService;
        this.dominantService = dominantService;
    }

    @Override
    protected SignalResultData<Change> processSignalInit(String namespace, String machineId, Value args) {
        log.info("Request processSignalInit() machineId: {} value: {}", machineId, args);
        List<Change> changes = toChangeList(args);
        Change statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createPendingStatus()));
        changes.add(statusChanged);
        SignalResultData<Change> resultData = new SignalResultData<>(
                toChangeList(toValue(changes)), buildComplexActionWithTimer(Timer.timeout(1), buildLastEventHistoryRange())
        );
        log.info("Response of processSignalInit: {}", resultData);
        return resultData;
    }

    @Override
    protected SignalResultData<Change> processSignalTimeout(String namespace, String machineId, List<TMachineEvent<Change>> tMachineEvents) {
        log.info("Request processSignalTimeout() machineId: {} list: {}", machineId, tMachineEvents);
        List<Change> changes = tMachineEvents.stream().map(TMachineEvent::getData).collect(Collectors.toList());
        Change lastChange = changes.get(changes.size() - 1);
        if (lastChange == null) {
            throw new IllegalArgumentException("Don't have events");
        }

        CashReg cashReg = listChangesToCashReg(changes);
        ProxyObject proxyObject = extractProxyObject(partyManagementService, dominantService, cashReg.getShopId(), cashReg.getPartyId());
        ProxyDefinition proxyDefinition = proxyObject.getData();

        // TODO: аггрегировать данные из настроек мерчанта
        String url = proxyDefinition.getUrl();
        Map<String, String> proxyOptions = proxyDefinition.getOptions();

        // TODO: create wrappers
        // TODO: add default timer
        CashRegContext context;
        ComplexAction complexAction = new ComplexAction();
        if (lastChange.isSetCreated()) {
            Change statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createPendingStatus()));
            changes.add(statusChanged);
        } else if (lastChange.isSetStatusChanged()) {
            Status status = lastChange.getStatusChanged().getStatus();
            Change statusChanged;
            if (status.isSetPending()) {
                SessionChangePayload sessionChangePayload = new SessionChangePayload();
                sessionChangePayload.setStarted(new SessionStarted());
                statusChanged = Change.session(new SessionChange().setPayload(sessionChangePayload));
                changes.add(statusChanged);
            } else if (status.isSetDelivered()) {
                statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createDeliveredStatus()));
                changes.add(statusChanged);
            } else {
                statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createFailedStatus()));
                changes.add(statusChanged);
            }
        } else if (lastChange.isSetSession()) {

            if (lastChange.getSession().getPayload().isSetStarted()) {
                Change statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createDeliveredStatus()));
                changes.add(statusChanged);
                complexAction = buildComplexActionWithTimer(Timer.timeout(1), buildLastEventHistoryRange());
            }

            if (lastChange.getSession().getPayload().isSetSessionAdapterStateChanged()) {
                context = prepareCashRegContext(cashReg, proxyOptions);
                CashRegProviderSrv.Iface prv = ProtoUtils.cashRegProviderSrv(url, 10);
                CashRegResult result;
                try {
                    result = prv.register(context);
                } catch (TException e) {
                    throw new IllegalStateException("Can't receive result");
                }

                SessionChangePayload sessionChangePayload = new SessionChangePayload();
                SessionAdapterStateChanged sessionAdapterStateChanged = new SessionAdapterStateChanged();
                sessionAdapterStateChanged.setState(com.rbkmoney.damsel.msgpack.Value.bin(result.getState()));
                if (result.getIntent().isSetSleep()) {

                    sessionChangePayload.setSessionAdapterStateChanged(sessionAdapterStateChanged);
                    Change statusChanged = Change.session(new SessionChange().setPayload(sessionChangePayload));
                    changes.add(statusChanged);

                    com.rbkmoney.machinegun.base.Timer timer = prepareTimer(result.getIntent().getSleep().getTimer());
                    complexAction = buildComplexActionWithTimer(timer, buildLastEventHistoryRange());
                }

                if (result.getIntent().isSetFinish()) {
                    FinishIntent finishIntent = result.getIntent().getFinish();
                    SessionFinished sessionFinished = new SessionFinished();
                    SessionResult sessionResult = new SessionResult();
                    if (finishIntent.getStatus().isSetFailure()) {
                        SessionFailed sessionFailed = new SessionFailed();
                        com.rbkmoney.damsel.cashreg.base.Failure failure = new com.rbkmoney.damsel.cashreg.base.Failure();
                        failure.setCode(result.getIntent().getFinish().getStatus().getFailure().getCode());
                        failure.setCode(result.getIntent().getFinish().getStatus().getFailure().getReason());
                        sessionFailed.setFailure(failure);
                        sessionResult.setFailed(sessionFailed);
                        sessionFinished.setResult(sessionResult);
                    } else {
                        SessionSucceeded sessionSucceeded = new SessionSucceeded();
                        sessionSucceeded.setInfo(result.getCashregInfo());
                        sessionResult.setSucceeded(sessionSucceeded);
                        sessionFinished.setResult(sessionResult);
                    }
                    sessionChangePayload.setFinished(sessionFinished);
                    Change statusChanged = Change.session(new SessionChange().setPayload(sessionChangePayload));
                    changes.add(statusChanged);
                }

                sessionChangePayload.setSessionAdapterStateChanged(new SessionAdapterStateChanged());
                Change statusChanged = Change.session(new SessionChange().setPayload(sessionChangePayload));
                changes.add(statusChanged);
            }

            if (lastChange.getSession().getPayload().isSetFinished()) {
                Change statusChanged = Change.status_changed(new StatusChange().setStatus(StatusCreators.createDeliveredStatus()));
                changes.add(statusChanged);
            }

            // complexAction need to use
        }

        SignalResultData<Change> resultData = new SignalResultData<>(toChangeList(toValue(changes)), complexAction);
        log.info("Response of processSignalTimeout: {}", resultData);
        return resultData;
    }

    public static com.rbkmoney.machinegun.base.Timer prepareTimer(com.rbkmoney.damsel.cashreg.base.Timer incomeTimer) {
        com.rbkmoney.machinegun.base.Timer timer = new com.rbkmoney.machinegun.base.Timer();
        if (incomeTimer.isSetTimeout()) {
            timer.setTimeout(incomeTimer.getTimeout());
        } else {
            timer.setDeadline(incomeTimer.getDeadline());
        }
        return timer;
    }

    /**
     * For repairer
     */
    @Override
    protected CallResultData<Change> processCall(String namespace, String machineId, Value args, List<TMachineEvent<Change>> tMachineEvents) {
        return new CallResultData<>(getLastEvent(tMachineEvents), Collections.emptyList(), new ComplexAction());
    }

    private Change getLastEvent(List<TMachineEvent<Change>> tMachineEvents) {
        if (tMachineEvents.isEmpty()) {
            return null;
        }
        return tMachineEvents.get(tMachineEvents.size() - 1).getData();
    }

}
