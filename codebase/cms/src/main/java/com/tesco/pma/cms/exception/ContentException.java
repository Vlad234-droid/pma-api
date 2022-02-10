package com.tesco.pma.cms.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class ContentException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 243330833393343940L;

    public ContentException(String code, String message) {
        super(code, message);
    }

    public ContentException(String code, String message, String field) {
        super(code, message, field);
    }

    public ContentException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
