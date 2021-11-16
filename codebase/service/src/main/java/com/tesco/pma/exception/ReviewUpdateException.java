package com.tesco.pma.exception;

public class ReviewUpdateException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 243330833393343712L;

    public ReviewUpdateException(String code, String message, String field) {
        super(code, message, field);
    }

    public ReviewUpdateException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
