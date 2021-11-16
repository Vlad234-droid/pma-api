package com.tesco.pma.exception;

public class ReviewCreationException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 243330833393343711L;

    public ReviewCreationException(String code, String message, String field) {
        super(code, message, field);
    }

    public ReviewCreationException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
