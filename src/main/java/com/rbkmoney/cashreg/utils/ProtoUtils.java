package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.Session;
import com.rbkmoney.damsel.cashreg.provider.SourceCreation;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtoUtils {

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

    private static SourceCreation prepareSourceCreation(CashReg cashreg) {
        SourceCreation sourceCreation = new SourceCreation();
        sourceCreation.setPayment(cashreg.getPaymentInfo());
        return sourceCreation;
    }

    public static CashRegContext prepareCashRegContext(CashReg cashReg, Map<String, String> proxyOptions) {
        CashRegContext context = new CashRegContext();
        context.setCashregId(cashReg.getCashregId());
        context.setAccountInfo(cashReg.getAccountInfo());
        context.setOptions(proxyOptions);
        context.setSession(new Session().setType(cashReg.getType()));
        context.setSourceCreation(prepareSourceCreation(cashReg));
        return context;
    }

    public static com.rbkmoney.machinegun.base.Timer prepareTimer(com.rbkmoney.damsel.cashreg.base.Timer incomingTimer) {
        com.rbkmoney.machinegun.base.Timer timer = new com.rbkmoney.machinegun.base.Timer();
        if (incomingTimer.isSetTimeout()) {
            timer.setTimeout(incomingTimer.getTimeout());
        } else {
            timer.setDeadline(incomingTimer.getDeadline());
        }
        return timer;
    }

    public static Value toValue(List<Change> changes) {
        List<Value> values = changes.stream().map(pc -> Value.bin(Geck.toMsgPack(pc))).collect(Collectors.toList());
        return Value.arr(values);
    }

    public static List<Change> toChangeList(Value value) {
        return value.getArr().stream().map(v -> Geck.msgPackToTBase(v.getBin(), Change.class)).collect(Collectors.toList());
    }

    public static CashReg mergeCashRegs(CashReg cashReg1, CashReg cashReg2) {

        if (cashReg2.getCashregProviderId() != null) {
            cashReg1.setCashregProviderId(cashReg2.getCashregProviderId());
        }

        if (cashReg2.getCashregId() != null) {
            cashReg1.setCashregId(cashReg2.getCashregId());
        }

        if (cashReg2.getPartyId() != null) {
            cashReg1.setPartyId(cashReg2.getPartyId());
        }

        if (cashReg2.getShopId() != null) {
            cashReg1.setShopId(cashReg2.getShopId());
        }

        if (cashReg2.getAccountInfo() != null) {
            cashReg1.setAccountInfo(cashReg2.getAccountInfo());
        }

        if (cashReg2.getPaymentInfo() != null) {
            cashReg1.setPaymentInfo(cashReg2.getPaymentInfo());
        }

        if (cashReg2.getType() != null) {
            cashReg1.setType(cashReg2.getType());
        }

        cashReg1.setStatus(cashReg2.getStatus());
        cashReg1.setPartyRevision(cashReg2.getPartyRevision());
        cashReg1.setDomainRevision(cashReg2.getDomainRevision());

        if (cashReg2.getInfo() != null) {
            cashReg1.setInfo(cashReg2.getInfo());
        }

        return cashReg1;
    }

}
