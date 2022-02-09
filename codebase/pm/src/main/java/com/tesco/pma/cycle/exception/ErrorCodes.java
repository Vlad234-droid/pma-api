package com.tesco.pma.cycle.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Cycle not found by uuid
     */
    PM_CYCLE_NOT_FOUND_BY_UUID,
    /**
     * Cycle not found by uuid and available statuses
     */
    PM_CYCLE_NOT_FOUND_BY_UUID_AND_STATUS,
    /**
     * Cycle not allowed to start
     */
    PM_CYCLE_NOT_ALLOWED_TO_START,
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
    PM_PARSE_IS_BLANK,
    /**
     * PM colleague cycle already exists
     */
    PM_COLLEAGUE_CYCLE_ALREADY_EXISTS,
    /**
     * There are more than one colleague cycles with the same status
     */
    PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS,
    /**
     * PM colleague does not cycle exist
     */
    PM_COLLEAGUE_CYCLE_NOT_EXIST,
    /**
     * Pm runtime process not found by uuid
     */
    PM_RT_PROCESS_NOT_FOUND_BY_UUID;

    @Override
    public String getCode() {
        return name();
    }
}
