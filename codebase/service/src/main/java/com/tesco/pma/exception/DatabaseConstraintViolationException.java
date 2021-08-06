package com.tesco.pma.exception;

public class DatabaseConstraintViolationException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 1342021766752413752L;

    public DatabaseConstraintViolationException(String code, String message, String field) {
        super(code, message, field);
    }

    public DatabaseConstraintViolationException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
