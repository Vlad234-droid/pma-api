package com.tesco.pma.process.api;

import com.tesco.pma.error.ErrorCodeAware;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 19:24
 */
public enum PMProcessErrorCodes implements ErrorCodeAware {
    /**
     * Process not found
     */
    PROCESS_NOT_FOUND,
    /**
     * Process already exists
     */
    PROCESS_ALREADY_EXISTS,
    /**
     * Value must be specified
     */
    VALUE_MUST_BE_SPECIFIED,
    /**
     * Specified value is wrong
     */
    WRONG_VALUE,
    /**
     * Process metadata not found
     */
    PROCESS_METADATA_NOT_FOUND,
    /**
     * Process metadata already exists
     */
    PROCESS_METADATA_ALREADY_EXISTS,

    /**
     * Resource not found
     */
    RESOURCE_NOT_FOUND,
    /**
     * Process not found by cycle uuid
     */
    PROCESS_NOT_FOUND_BY_CYCLE,
    ;


    @Override
    public String getCode() {
        return name();
    }
}
