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
     * Cycle not found for colleague
     */
    PM_CYCLE_NOT_FOUND_COLLEAGUE,
    /**
     * Cycle not found for status update
     */
    PM_CYCLE_NOT_FOUND_FOR_STATUS_UPDATE,
    /**
     * Cycle already exists
     */
    PM_CYCLE_ALREADY_EXISTS,
    /**
     * Cycle's metadata was not found
     */
    PM_CYCLE_METADATA_NOT_FOUND,
    /**
     * Associated dictionary item or resource was not found
     */
    PM_PARSE_NOT_FOUND,
    /**
     * Process name was not found in metadata or empty
     */
    PROCESS_NAME_IS_EMPTY,
    /**
     * Process execution exception
     */
    PROCESS_EXECUTION_EXCEPTION,
    /**
     * The passed value cannot be blank
     */
    PM_PARSE_IS_BLANK;

    @Override
    public String getCode() {
        return name();
    }
}
