package com.tesco.pma.exception;

public class InvalidParameterException extends AbstractApiRuntimeException {
    private static final long serialVersionUID = 1342021766752413741L;

    public InvalidParameterException(String code, String message, String field) {
        super(code, message, field);
    }

    public InvalidParameterException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
