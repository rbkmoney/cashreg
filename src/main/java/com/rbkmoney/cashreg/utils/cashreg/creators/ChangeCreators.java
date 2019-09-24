package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.*;

public class ChangeCreators {

    // Change
    public static Change createStatusChangePending() {
        return createStatusChanged(StatusCreators.createPendingStatus());
    }

    public static Change createStatusChangeDelivered() {
        return createStatusChanged(StatusCreators.createDeliveredStatus());
    }

    public static Change createStatusChangeFailed() {
        return createStatusChanged(StatusCreators.createFailedStatus());
    }

    public static Change createStatusChanged(Status status) {
        return Change.status_changed(new StatusChange().setStatus(status));
    }

    // Session
    public static Change createSessionChangeStarted() {
        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        sessionChangePayload.setStarted(new SessionStarted());
        return createSessionChange(sessionChangePayload);
    }

    public static Change createSessionChange(SessionChangePayload payload) {
        return Change.session(new SessionChange().setPayload(payload));
    }

}
