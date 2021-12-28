package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class ColleagueNotFoundException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = -2402907286181904104L;

    public ColleagueNotFoundException(String code, String message) {
        super(code, message);
    }

    public ColleagueNotFoundException(String code, String message, String field) {
        super(code, message, field);
    }

    public ColleagueNotFoundException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}