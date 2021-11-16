package com.tesco.pma.exception;

public class ReviewDeletionException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 243330833393343713L;

    public ReviewDeletionException(String code, String message, String field) {
        super(code, message, field);
    }

    public ReviewDeletionException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
