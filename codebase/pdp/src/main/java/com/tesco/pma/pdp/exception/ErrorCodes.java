package com.tesco.pma.pdp.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {
    /**
     * PDP Goal not found by id
     */
    PDP_GOAL_NOT_FOUND_BY_ID,
    /**
     * PDP Goal not found by colleague
     */
    PDP_GOAL_NOT_FOUND_BY_COLLEAGUE,
    /**
     * PDP Goal not found by colleague and number
     */
    PDP_GOAL_NOT_FOUND_BY_COLLEAGUE_AND_NUMBER,
    /**
     * PDP already exists by colleague and number
     */
    PDP_ALREADY_EXISTS;

    @Override
    public String getCode() {
        return name();
    }
}