package com.tesco.pma.notes.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum NotesErrorCodes implements ErrorCodeAware {

    NOTE_NOT_FOUND,
    NOTE_OWNER_REFERENCE_COLLISION,
    NOT_A_LINE_MANAGER,
    FOLDER_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
