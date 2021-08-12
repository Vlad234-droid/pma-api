package com.tesco.pma.exception;

public class NotFoundException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 243330833393343940L;

    public NotFoundException(String code, String message) {
        super(code, message);
    }

    public NotFoundException(String code, String message, String field) {
        super(code, message, field);
    }

    public NotFoundException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
