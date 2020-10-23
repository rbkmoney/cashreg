package com.rbkmoney.cashreg.service.management.converter;

import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.msgpack.Value;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class StateResultWrapper {
    private Value value;
    private CashregResult result;
}
