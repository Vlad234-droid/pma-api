package com.tesco.pma.cycle.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Cycle configuration not found by uuid
     */
    CYCLE_CONFIGURATION_NOT_FOUND_BY_UUID,
    /**
     * Cycle configurations not found
     */
    CYCLE_CONFIGURATIONS_NOT_FOUND,
    /**
     * Cycle configuration not found for status update
     */
    CYCLE_CONFIGURATION_NOT_FOUND_FOR_STATUS_UPDATE,
    /**
     * Cycle configuration already exists
     */
    CYCLE_CONFIGURATION_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }
}
