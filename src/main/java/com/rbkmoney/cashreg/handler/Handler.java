package com.rbkmoney.cashreg.handler;

public interface Handler<T, E> {

    default boolean accept(T change) {
        return getChangeType().getFilter().match(change);
    }

    void handle(T change, E event, String sourceId);

    ChangeType getChangeType();

}
