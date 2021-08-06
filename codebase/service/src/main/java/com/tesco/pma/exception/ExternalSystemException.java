package com.tesco.pma.exception;

/**
 * Exception for errors in communication/integration with external services, e.g. Colleague-Api, etc.
 */
public class ExternalSystemException extends AbstractApiRuntimeException {
    public ExternalSystemException(String code, String message) {
        super(code, message);
    }

    public ExternalSystemException(String code, String message, Throwable cause) {
        super(code, message, null, cause);
    }
}
