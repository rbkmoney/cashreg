package com.rbkmoney.cashreg.service.exception;

public class UnsupportedMethodException extends RuntimeException {

    public UnsupportedMethodException(String message) {
        super(message);
    }

    public UnsupportedMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMethodException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
