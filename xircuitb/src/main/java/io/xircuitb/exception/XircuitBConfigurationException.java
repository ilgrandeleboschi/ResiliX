package io.xircuitb.exception;

import io.resilix.exception.ResiliXException;

public class XircuitBConfigurationException extends ResiliXException {

    public XircuitBConfigurationException(String message) {
        super(message);
    }

    public XircuitBConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
