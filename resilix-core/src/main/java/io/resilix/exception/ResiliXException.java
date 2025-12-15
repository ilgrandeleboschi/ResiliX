package io.resilix.exception;

public class ResiliXException extends RuntimeException {

    public ResiliXException(String message) {
        super(message);
    }

    public ResiliXException(String message, Throwable cause) {
        super(message, cause);
    }

}