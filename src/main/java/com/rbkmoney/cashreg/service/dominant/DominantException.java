package com.rbkmoney.cashreg.service.dominant;

public class DominantException extends RuntimeException {

    public DominantException() {
        super();
    }

    public DominantException(String message) {
        super(message);
    }

    public DominantException(Throwable cause) {
        super(cause);
    }

    public DominantException(String message, Throwable cause) {
        super(message, cause);
    }

}
