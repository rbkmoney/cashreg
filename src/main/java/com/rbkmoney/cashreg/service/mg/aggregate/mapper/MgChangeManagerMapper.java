package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface.Mapper;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MgChangeManagerMapper {

    private final List<Mapper> changeMappers;

    public CashReg handle(Change change) {
        return changeMappers.stream()
                .filter(mapper -> mapper.filter(change))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find mapper"))
                .map(change);
    }

    public CashReg process(List<Change> changes) {
        return changes.stream().map(this::handle).reduce(new CashReg(), ProtoUtils::mergeCashRegs);
    }

}
