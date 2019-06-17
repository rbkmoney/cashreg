package com.rbkmoney.cashreg.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.springframework.kafka.support.Acknowledgment;

public interface MachineEventHandler {

    void handle(MachineEvent message, Acknowledgment ack);

}
