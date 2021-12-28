package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class DuplicatedAccountException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 5157307634800984801L;

    public DuplicatedAccountException(String code, String message) {
        super(code, message);
    }

    public DuplicatedAccountException(String code, String message, String field) {
        super(code, message, field);
    }

    public DuplicatedAccountException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
