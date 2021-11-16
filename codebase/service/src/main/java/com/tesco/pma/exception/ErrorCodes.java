package com.tesco.pma.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * File not found
     */
    ERROR_FILE_NOT_FOUND,

    MESSAGE_NOT_READABLE_EXCEPTION,
    /**
     * Required request part is not present
     */
    MISSING_SERVLET_REQUEST_PART_EXCEPTION,
    /**
     * Type mismatch
     */
    TYPE_MISMATCH,
    /**
     * Method argument not valid
     */
    METHOD_ARGUMENT_NOT_VALID,
    /**
     * Method argument type mismatch
     */
    METHOD_ARGUMENT_TYPE_MISMATCH,
    /**
     * Unsupported media type
     */
    UNSUPPORTED_MEDIA_TYPE,
    /**
     * Constraint violation
     */
    CONSTRAINT_VIOLATION,
    /**
     * Unique constraint violation
     */
    UNIQUE_CONSTRAINT_VIOLATION,
    /**
     * Unexpected error
     */
    ER_CODE_UNEXPECTED_EXCEPTION,

    /**
     * User not authenticated
     */
    USER_NOT_AUTHENTICATED,
    /**
     * User not authorized
     */
    USER_NOT_AUTHORIZED,
    /**
     * Request limit exceeded
     */
    LIMIT_EXCEEDED,
    /**
     * Unauthenticated
     */
    UNAUTHENTICATED,
    /**
     * Access Denied
     */
    ACCESS_DENIED,

    /**
     * Failed to establish connection with datasource
     */
    DB_CONNECTION_ERROR,
    /**
     * External API connection error
     */
    EXTERNAL_API_CONNECTION_ERROR,
    /**
     * User not found
     */
    USER_NOT_FOUND,
    /**
     * Colleague Api unexpected result.
     */
    COLLEAGUE_API_UNEXPECTED_RESULT,
    /**
     * Failed to accept traffic
     */
    READINESS_STATE_ERROR,
    /**
     * Event accepted but not published (e.g. it is a duplicate)
     */
    EVENT_NOT_PUBLISHED,
    /**
     * Event payload doesn't conform to its JSON Schema
     */
    EVENT_PAYLOAD_ERROR,
    /**
     * Event unexpected error
     */
    EVENT_UNEXPECTED_ERROR,
    /**
     * Application is not working
     */
    LIVENESS_STATE_ERROR,
    /**
     * Executed process was failed
     */
    PROCESSING_FAILED;

    @Override
    public String getCode() {
        return name();
    }
}
