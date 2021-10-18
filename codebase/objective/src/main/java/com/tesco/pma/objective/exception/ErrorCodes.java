package com.tesco.pma.objective.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Review not found by uuid
     */
    REVIEW_NOT_FOUND_BY_UUID,
    /**
     * Review not found for colleague
     */
    REVIEW_NOT_FOUND_FOR_COLLEAGUE,
    /**
     * Reviews not found
     */
    REVIEWS_NOT_FOUND,
    /**
     * Reviews not found for status update
     */
    REVIEWS_NOT_FOUND_FOR_STATUS_UPDATE,
    /**
     * Review already exists
     */
    REVIEW_ALREADY_EXISTS,
    /**
     * Business unit does not exists
     */
    BUSINESS_UNIT_NOT_EXISTS,
    /**
     * Group objective already exists
     */
    GROUP_OBJECTIVE_ALREADY_EXISTS,
    /**
     * Group objectives not found
     */
    GROUP_OBJECTIVES_NOT_FOUND;

    @Override
    public String getCode() {
        return name();
    }
}
