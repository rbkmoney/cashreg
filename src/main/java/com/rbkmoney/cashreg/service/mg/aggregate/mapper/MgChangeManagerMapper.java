package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MgChangeManagerMapper {

    private final List<ChangeMapper> changeMappers;

    public CashReg handle(Change change) {
        return changeMappers.stream()
                .filter(mapper -> mapper.filter(change))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find mapper"))
                .map(change);
    }

    public CashReg process(List<Change> changes) {
        return changes.stream()
                .map(this::handle)
                .reduce(new CashReg(),
                        (cashReg1, cashReg2) -> {

                            if (cashReg2.getId() != null) {
                                cashReg1.setId(cashReg2.getId());
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

                            if (cashReg2.getInfo() != null) {
                                cashReg1.setInfo(cashReg2.getInfo());
                            }

                            return cashReg1;
                        });
    }

}
