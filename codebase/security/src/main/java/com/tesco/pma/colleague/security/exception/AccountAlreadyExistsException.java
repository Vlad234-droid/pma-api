package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class AccountAlreadyExistsException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 954038103497897125L;


    public AccountAlreadyExistsException(String code, String message) {
        super(code, message);
    }

    public AccountAlreadyExistsException(String code, String message, String field) {
        super(code, message, field);
    }

    public AccountAlreadyExistsException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
