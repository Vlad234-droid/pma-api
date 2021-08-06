package com.tesco.pma.exception;

public class EventPublishException extends AbstractApiRuntimeException {
    public EventPublishException(String code, String message) {
        super(code, message);
    }
}
