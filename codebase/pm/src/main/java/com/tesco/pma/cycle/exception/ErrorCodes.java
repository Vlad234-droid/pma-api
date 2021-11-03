package com.tesco.pma.cycle.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Cycle not found by uuid
     */
    PM_CYCLE_NOT_FOUND_BY_UUID,
    /**
     * Cycle not found
     */
    PM_CYCLE_NOT_FOUND,
    /**
     * Cycle not found for status update
     */
    PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
    /**
     * Cycle already exists
     */
    PM_CYCLE_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }
}
