package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class AccountNotFoundException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = -6825493895807590189L;

    public AccountNotFoundException(String code, String message) {
        super(code, message);
    }

    public AccountNotFoundException(String code, String message, String field) {
        super(code, message, field);
    }

    public AccountNotFoundException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
