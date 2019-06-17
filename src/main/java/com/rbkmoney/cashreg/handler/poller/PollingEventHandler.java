package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.cashreg.handler.Handler;

public interface PollingEventHandler extends Handler<InvoiceChange, MachineEvent> {
}
