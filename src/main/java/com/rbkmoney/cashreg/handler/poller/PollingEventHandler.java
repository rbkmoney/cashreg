package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.handler.Handler;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;

public interface PollingEventHandler extends Handler<InvoiceChange> {

}
