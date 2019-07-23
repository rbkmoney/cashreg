package com.rbkmoney.cashreg.handler;

public interface Handler<T> {

    default boolean accept(T change) {
        return getChangeType().getFilter().match(change);
    }

    void handle(T change, String sourceId);

    ChangeType getChangeType();

}
