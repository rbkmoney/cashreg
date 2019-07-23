package com.rbkmoney.cashreg.service.exception;

public class UnsuccessfulSaveException extends RuntimeException {

    public UnsuccessfulSaveException() {
        super();
    }

    public UnsuccessfulSaveException(String message) {
        super(message);
    }
}
