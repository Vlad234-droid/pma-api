package com.tesco.pma.reports.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * All system download errors
     */
    INTERNAL_DOWNLOAD_ERROR,

    /**
     * Review not found
     */
    REVIEW_REPORT_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}