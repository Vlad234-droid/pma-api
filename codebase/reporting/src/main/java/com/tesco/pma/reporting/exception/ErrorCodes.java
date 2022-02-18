package com.tesco.pma.reporting.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * All system download errors
     */
    INTERNAL_DOWNLOAD_ERROR,

    /**
     * Report not found
     */
    REPORT_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}