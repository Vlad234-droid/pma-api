package com.tesco.pma.colleague.security.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class DuplicatedRoleException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = -7742204105636988511L;

    public DuplicatedRoleException(String code, String message) {
        super(code, message);
    }

    public DuplicatedRoleException(String code, String message, String field) {
        super(code, message, field);
    }

    public DuplicatedRoleException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }

}
