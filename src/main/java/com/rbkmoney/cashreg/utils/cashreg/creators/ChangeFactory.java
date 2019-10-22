package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.cashreg.status.Delivered;
import com.rbkmoney.damsel.cashreg.status.Failed;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeFactory {

    // Change
    public static Change createStatusChangePending() {
        return createStatusChanged(Status.pending(new Pending()));
    }

    public static Change createStatusChangeDelivered() {
        return createStatusChanged(Status.delivered(new Delivered()));
    }

    public static Change createStatusChangeFailed() {
        return createStatusChanged(Status.failed(new Failed()));
    }

    public static Change createStatusChanged(Status status) {
        return Change.status_changed(new StatusChange().setStatus(status));
    }

    // Session
    public static Change createSessionChangeStarted() {
        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        sessionChangePayload.setStarted(new SessionStarted());
        String sessionId = UUID.randomUUID().toString();
        return createSessionChange(sessionId, sessionChangePayload);
    }

    public static Change createSessionChange(SessionChangePayload payload) {
        return Change.session(new SessionChange().setPayload(payload));
    }

    public static Change createSessionChange(String sessionId, SessionChangePayload payload) {
        return Change.session(new SessionChange().setId(sessionId).setPayload(payload));
    }

}
