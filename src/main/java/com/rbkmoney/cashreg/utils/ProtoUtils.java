package com.rbkmoney.cashreg.utils;

import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ProtoUtils {

    public static Change toCashRegCreated(CashRegParams params) {
        CreatedChange created = new CreatedChange();
        CashReg cashReg = new CashReg();
        cashReg.setId(params.getId());
        cashReg.setPaymentInfo(params.getPaymentInfo());
        cashReg.setType(params.getType());
        cashReg.setShopId(params.getShopId());
        cashReg.setPartyId(params.getPartyId());
        cashReg.setStatus(StatusCreators.createPendingStatus());
        created.setCashreg(cashReg);
        return Change.created(created);
    }

    /**
     * TODO: need test for list
     * @param change
     * @return
     */
    public static CashReg toCashReg(Change change) {
        CashReg cashReg = new CashReg();

        if (change == null) {
            throw new IllegalArgumentException("CashReg change");
        }

        if (change.isSetCreated()) {
            return change.getCreated().getCashreg();
        } else if (change.isSetStatusChanged()) {
            cashReg.setStatus(change.getStatusChanged().getStatus());
        } else if (change.isSetSession()) {
            prepareSessionStatus(change, cashReg);
        } else {
            throw new IllegalArgumentException("Wrong CashReg change");
        }
        return cashReg;
    }

    private static void prepareSessionStatus(Change change, CashReg cashReg) {
        if (change.getSession().getPayload().isSetFinished()) {
            SessionFinished sessionFinished = change.getSession().getPayload().getFinished();
            SessionResult sessionResult = sessionFinished.getResult();
            if (sessionResult.isSetFailed()) {
                cashReg.setStatus(StatusCreators.createFailedStatus());
            }
            if (sessionResult.isSetSucceeded()) {
                cashReg.setStatus(StatusCreators.createDeliveredStatus());
            }
        } else {
            cashReg.setStatus(StatusCreators.createPendingStatus());
        }
    }

    public static ComplexAction buildComplexActionWithDeadline(Instant deadline, HistoryRange historyRange) {
        return buildComplexActionWithTimer(Timer.deadline(deadline.toString()), historyRange);
    }

    public static ComplexAction buildComplexActionWithTimer(Timer timer, HistoryRange historyRange) {
        SetTimerAction setTimerAction = new SetTimerAction().setTimer(timer).setRange(historyRange);
        return new ComplexAction().setTimer(TimerAction.set_timer(setTimerAction));
    }

    public static HistoryRange buildLastEventHistoryRange() {
        HistoryRange historyRange = new HistoryRange();
        historyRange.setDirection(Direction.backward);
        historyRange.setLimit(1);
        return historyRange;
    }

    public static Value toValue(List<Change> changes) {
        List<Value> values = changes.stream().map(pc -> Value.bin(Geck.toMsgPack(pc))).collect(Collectors.toList());
        return Value.arr(values);
    }

    public static List<Change> toChangeList(Value value) {
        return value.getArr().stream().map(v -> Geck.msgPackToTBase(v.getBin(), Change.class)).collect(Collectors.toList());
    }
}
