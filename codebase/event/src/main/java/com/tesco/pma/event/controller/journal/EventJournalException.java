package com.tesco.pma.event.controller.journal;

public class EventJournalException extends Exception {

    private static final long serialVersionUID = 3897068710135068141L;

    public EventJournalException(String message) {
        super(message);
    }

    public EventJournalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventJournalException(Throwable cause) {
        super(cause);
    }

}
