package com.tesco.pma.exception;

public class InvalidPayloadException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 6371845018489554449L;

    public InvalidPayloadException(String code, String message, String field) {
        super(code, message, field);
    }

    public InvalidPayloadException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
