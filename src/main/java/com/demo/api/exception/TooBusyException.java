package com.demo.api.exception;

public class TooBusyException extends RuntimeException {
    public TooBusyException() {
    }

    public TooBusyException(String message) {
        super(message);
    }

    public TooBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooBusyException(Throwable cause) {
        super(cause);
    }

    public TooBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
