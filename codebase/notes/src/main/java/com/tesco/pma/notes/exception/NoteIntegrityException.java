package com.tesco.pma.notes.exception;

import com.tesco.pma.exception.AbstractApiRuntimeException;

public class NoteIntegrityException extends AbstractApiRuntimeException {

    private static final long serialVersionUID = 243330833393343940L;

    public NoteIntegrityException(String code, String message) {
        super(code, message);
    }

    public NoteIntegrityException(String code, String message, String field) {
        super(code, message, field);
    }

    public NoteIntegrityException(String code, String message, String field, Throwable cause) {
        super(code, message, field, cause);
    }
}
