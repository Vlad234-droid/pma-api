package com.tesco.pma.exceptions;

/**
 * Exception during initialization of object.
 */
public class InitializationException extends Exception {

    private static final long serialVersionUID = 6839669896061280596L;

    public InitializationException() {
    }

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(Throwable cause) {
        super(cause);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
