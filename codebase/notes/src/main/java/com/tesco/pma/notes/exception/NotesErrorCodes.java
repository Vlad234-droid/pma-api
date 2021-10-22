package com.tesco.pma.notes.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum NotesErrorCodes implements ErrorCodeAware {

    NOTE_NOT_FOUND,
    FOLDER_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
