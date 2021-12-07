package com.tesco.pma.event.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class EventSendingException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 1249199824898599195L;

    public EventSendingException(String code, String message) {
        super(code, message);
    }

    public EventSendingException(String code, String message, String field) {
        super(code, message, field);
    }

    public EventSendingException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
