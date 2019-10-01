package com.rbkmoney.cashreg.service.mg.aggregate.handler;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MgChangeHandler {

    private final List<ChangeHandler> changeHandlers;

    public CashReg handle(Change change) {
        return changeHandlers.stream()
                .filter(handler -> handler.filter(change))
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .handle(change);
    }

    public CashReg listChangesToCashReg(List<Change> changes) {
        return changes.stream().reduce(new CashReg(),
                (cashReg, change) -> handle(change),
                (cashRegCurrent, cashRegNew) -> cashRegCurrent.setStatus(cashRegNew.getStatus())
        );
    }

}
