package com.tesco.pma.notes.exception;

import org.springframework.dao.UncategorizedDataAccessException;

public class UnknownDataManipulationException extends UncategorizedDataAccessException {
    /**
     * Constructor for UncategorizedDataAccessException.
     *
     * @param msg   the detail message
     */
    public UnknownDataManipulationException(String msg) {
        super(msg, null);
    }
}
