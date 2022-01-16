package com.tesco.pma.reporting.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Review not found
     */
    REVIEW_REPORT_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}