package com.rbkmoney.cashreg.service.dominant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ResponseDominantWrapper<R> {
    private R response;
    private long revisionVersion;
}
