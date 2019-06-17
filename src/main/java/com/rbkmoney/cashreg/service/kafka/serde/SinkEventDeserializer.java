package com.rbkmoney.cashreg.service.kafka.serde;

import com.rbkmoney.kafka.common.deserializer.AbstractDeserializerAdapter;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SinkEventDeserializer extends AbstractDeserializerAdapter<SinkEvent> {

    @Override
    public SinkEvent deserialize(String topic, byte[] data) {
        return this.deserialize(data, new SinkEvent());
    }

}