package com.tesco.pma.event.controller;

import com.tesco.pma.event.Event;

public class EventException extends Exception {

    private static final long serialVersionUID = -2894995107109522552L;
    protected String message;
    protected Event event;

    public EventException() {
        super();
    }

    public EventException(Throwable cause) {
        super(cause);
    }

    public EventException(Event event, String message) {
        super(message);
        this.message = message;
        this.event = event;
    }

    public EventException(Event event, String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.event = event;
    }

    public Event getEventName() {
        return event;
    }

    public void setEventName(Event event) {
        this.event = event;
    }
}
