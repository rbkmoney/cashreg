package com.rbkmoney.cashreg.service.provider;

import com.rbkmoney.cashreg.domain.SourceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Source {

    private final CashRegProvider cashRegProvider;

    private final SourceType sourceType;

}
