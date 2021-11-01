package com.tesco.pma.cycle.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Cycle not found by uuid
     */
    CYCLE_NOT_FOUND_BY_UUID,
    /**
     * Cycles not found
     */
    CYCLES_NOT_FOUND,
    /**
     * Cycles not found for status update
     */
    CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
    /**
     * Cycle already exists
     */
    CYCLE_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }
}
