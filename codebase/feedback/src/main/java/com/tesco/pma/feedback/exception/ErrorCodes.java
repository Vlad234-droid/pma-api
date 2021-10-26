package com.tesco.pma.feedback.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Entity not found
     */
    FEEDBACK_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
