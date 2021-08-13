package com.tesco.pma.event;

public class EventSendingException extends RuntimeException {

    private static final long serialVersionUID = 1249199824898599195L;

    public EventSendingException() {
        super();
    }

    public EventSendingException(Throwable throwable) {
        super(throwable);
    }

    public EventSendingException(final String message) {
        super(message);
    }

    public EventSendingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
