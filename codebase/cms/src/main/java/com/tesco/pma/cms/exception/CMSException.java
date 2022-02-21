package com.tesco.pma.cms.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class CMSException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 243330833393343940L;

    public CMSException(String code, String message) {
        super(code, message);
    }

    public CMSException(String code, String message, String field) {
        super(code, message, field);
    }

    public CMSException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
