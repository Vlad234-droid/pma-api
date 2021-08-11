package com.tesco.pma.bpm.api;

import java.io.Serializable;

/**
 * Wrap all exception raised during process execution
 */
public class ProcessExecutionException extends Exception implements Serializable {

    private static final long serialVersionUID = 4277182468034401973L;

    /**
     * Wrap {@code e} in new instance of {@link ProcessExecutionException}
     * @param throwable
     */
    public ProcessExecutionException(Throwable throwable) {
        super(throwable);
    }

    public ProcessExecutionException(String message) {
        super(message);
    }

    public ProcessExecutionException() {
        super();
    }

    public ProcessExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
