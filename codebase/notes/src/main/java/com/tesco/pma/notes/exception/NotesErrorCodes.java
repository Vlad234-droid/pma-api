package com.tesco.pma.notes.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum NotesErrorCodes implements ErrorCodeAware {

    NOTE_NOT_FOUND,
    NOTE_OWNER_REFERENCE_COLLISION,
    NOTE_HAS_NOT_BEEN_CREATED,
    NOTE_INTEGRITY_VIOLATION,
    NOT_A_LINE_MANAGER,
    FOLDER_NOT_FOUND,
    FOLDER_HAS_NOT_BEEN_CREATED,
    FOLDER_INTEGRITY_VIOLATION;

    @Override
    public String getCode() {
        return name();
    }
}
