package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.cashreg.status.Delivered;
import com.rbkmoney.damsel.cashreg.status.Failed;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;

public class StatusCreators {

    public static Status createPendingStatus() {
        return Status.pending(new Pending());
    }

    public static Status createDeliveredStatus() {
        return Status.delivered(new Delivered());
    }

    public static Status createFailedStatus() {
        return Status.failed(new Failed());
    }

}
