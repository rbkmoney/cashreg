package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.adapter.CashregContext;
import com.rbkmoney.damsel.cashreg.adapter.Session;
import com.rbkmoney.damsel.cashreg.adapter.SourceCreation;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtoUtils {

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

    private static SourceCreation prepareSourceCreation(Receipt receipt) {
        SourceCreation sourceCreation = new SourceCreation();
        sourceCreation.setPayment(receipt.getPaymentInfo());
        return sourceCreation;
    }

    public static CashregContext prepareCashRegContext(Receipt receipt, Map<String, String> proxyOptions) {
        CashregContext context = new CashregContext();
        context.setCashregId(receipt.getReceiptId());
        context.setAccountInfo(receipt.getAccountInfo());
        context.setOptions(proxyOptions);
        context.setSession(new Session().setType(receipt.getType()));
        context.setSourceCreation(prepareSourceCreation(receipt));
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

    public static Receipt mergeCashRegs(Receipt cashReg1, Receipt cashReg2) {

        if (cashReg2.getCashregProvider() != null) {
            cashReg1.setCashregProvider(cashReg2.getCashregProvider());
        }

        if (cashReg2.getReceiptId() != null) {
            cashReg1.setReceiptId(cashReg2.getReceiptId());
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
