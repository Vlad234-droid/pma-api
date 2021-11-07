package com.tesco.pma.feedback.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Feedback not found
     */
    FEEDBACK_NOT_FOUND,

    /**
     * Feedback item not found
     */
    FEEDBACK_ITEM_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
