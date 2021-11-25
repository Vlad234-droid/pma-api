package com.tesco.pma.review.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Review not found
     */
    REVIEW_NOT_FOUND,
    /**
     * Review already exists
     */
    REVIEW_ALREADY_EXISTS,
    /**
     * Organisation objective already exists
     */
    ORG_OBJECTIVE_ALREADY_EXISTS,
    /**
     * Organisation objectives not found
     */
    ORG_OBJECTIVES_NOT_FOUND,
    /**
     * Allowed statuses not found
     */
    ALLOWED_STATUSES_NOT_FOUND,
    /**
     * Status not allowed for operation
     */
    REVIEW_STATUS_NOT_ALLOWED,
    /**
     * Max review's number limit reached
     */
    MAX_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
    /**
     * Min review's number limit reached
     */
    MIN_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
    /**
     * Min review's number limit reached
     */
    CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT;

    @Override
    public String getCode() {
        return name();
    }
}
