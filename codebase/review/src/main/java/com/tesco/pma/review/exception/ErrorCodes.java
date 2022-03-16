package com.tesco.pma.review.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Review not found
     */
    REVIEW_NOT_FOUND,
    /**
     * Colleague cycle not found
     */
    COLLEAGUE_CYCLE_NOT_FOUND,
    /**
     * Timeline point cycle not found
     */
    TIMELINE_POINT_NOT_FOUND,
    /**
     * Timeline point already exists
     */
    TIMELINE_POINT_ALREADY_EXISTS,
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
    CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT,
    /**
     * Objective sharing already enabled
     */
    OBJECTIVE_SHARING_ALREADY_ENABLED,
    /**
     * Objective sharing was not enabled
     */
    OBJECTIVE_SHARING_NOT_ENABLED,
    /**
     * Cannot read file as not a manager of colleague
     */
    INSUFFICIENT_FILE_ACCESS;

    @Override
    public String getCode() {
        return name();
    }
}
