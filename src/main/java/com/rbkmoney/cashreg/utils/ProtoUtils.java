package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.Session;
import com.rbkmoney.damsel.cashreg.provider.SourceCreation;
import com.rbkmoney.damsel.cashreg.type.Type;
import com.rbkmoney.damsel.cashreg_domain.PaymentInfo;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
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
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCart(cashreg.getPaymentInfo().getCart());
        paymentInfo.setCash(cashreg.getPaymentInfo().getCash());
        paymentInfo.setEmail(cashreg.getPaymentInfo().getEmail());
        sourceCreation.setPayment(paymentInfo);
        return sourceCreation;
    }

    private static Session prepareSession(Type type) {
        Session session = new Session();
        session.setType(type);
        return session;
    }

    public static CashRegContext prepareCashRegContext(CashReg cashReg, Map<String, String> proxyOptions) {
        CashRegContext context = new CashRegContext();
        context.setAccountInfo(cashReg.getAccountInfo());
        context.setOptions(proxyOptions);

        Session session = prepareSession(cashReg.getType());
        context.setSession(session);

        SourceCreation sourceCreation = prepareSourceCreation(cashReg);
        context.setSourceCreation(sourceCreation);
        return context;
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

    public static Value toValue(List<Change> changes) {
        List<Value> values = changes.stream().map(pc -> Value.bin(Geck.toMsgPack(pc))).collect(Collectors.toList());
        return Value.arr(values);
    }

    public static List<Change> toChangeList(Value value) {
        return value.getArr().stream().map(v -> Geck.msgPackToTBase(v.getBin(), Change.class)).collect(Collectors.toList());
    }

}
