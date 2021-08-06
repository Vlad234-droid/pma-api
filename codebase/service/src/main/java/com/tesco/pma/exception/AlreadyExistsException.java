package com.tesco.pma.exception;

/**
 * Resource already exists.
 */
public class AlreadyExistsException extends AbstractApiRuntimeException {

    public AlreadyExistsException(String code, String message) {
        super(code, message);
    }

    public AlreadyExistsException(String code, String message, Throwable cause) {
        super(code, message, null, cause);
    }
}
