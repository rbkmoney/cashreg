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

    public static final int HISTORY_RANGE_DEFAULT_LIMIT = 10;

    public static ComplexAction buildComplexActionWithTimer(Timer timer, HistoryRange historyRange) {
        SetTimerAction setTimerAction = new SetTimerAction().setTimer(timer).setRange(historyRange);
        return new ComplexAction().setTimer(TimerAction.set_timer(setTimerAction));
    }

    public static HistoryRange buildLastEventHistoryRange() {
        return buildDirectionBackwardEventHistoryRange(1);
    }

    public static HistoryRange buildDirectionBackwardEventHistoryRange(int limit) {
        HistoryRange historyRange = new HistoryRange();
        historyRange.setDirection(Direction.backward);
        historyRange.setLimit(limit);
        return historyRange;
    }

    public static HistoryRange buildDirectionBackwardEventHistoryRange() {
        return buildDirectionBackwardEventHistoryRange(HISTORY_RANGE_DEFAULT_LIMIT);
    }

    private static SourceCreation prepareSourceCreation(Receipt receipt) {
        SourceCreation sourceCreation = new SourceCreation();
        sourceCreation.setPayment(receipt.getPaymentInfo());
        return sourceCreation;
    }

    public static CashregContext prepareCashRegContext(Receipt receipt, Map<String, String> proxyOptions) {
        return new CashregContext()
                .setCashregId(receipt.getReceiptId())
                .setAccountInfo(receipt.getAccountInfo())
                .setOptions(proxyOptions)
                .setSession(new Session().setType(receipt.getType()))
                .setSourceCreation(prepareSourceCreation(receipt));
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

    public static Receipt mergeReceipts(Receipt receipt1, Receipt receipt2) {

        if (receipt2.getCashregProvider() != null) {
            receipt1.setCashregProvider(receipt2.getCashregProvider());
        }

        if (receipt2.getReceiptId() != null) {
            receipt1.setReceiptId(receipt2.getReceiptId());
        }

        if (receipt2.getPartyId() != null) {
            receipt1.setPartyId(receipt2.getPartyId());
        }

        if (receipt2.getShopId() != null) {
            receipt1.setShopId(receipt2.getShopId());
        }

        if (receipt2.getAccountInfo() != null) {
            receipt1.setAccountInfo(receipt2.getAccountInfo());
        }

        if (receipt2.getPaymentInfo() != null) {
            receipt1.setPaymentInfo(receipt2.getPaymentInfo());
        }

        if (receipt2.getType() != null) {
            receipt1.setType(receipt2.getType());
        }

        receipt1.setStatus(receipt2.getStatus());
        receipt1.setPartyRevision(receipt2.getPartyRevision());
        receipt1.setDomainRevision(receipt2.getDomainRevision());

        if (receipt2.getInfo() != null) {
            receipt1.setInfo(receipt2.getInfo());
        }

        return receipt1;
    }

}
