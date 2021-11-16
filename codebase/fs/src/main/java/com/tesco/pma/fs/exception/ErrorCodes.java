package com.tesco.pma.fs.exception;

import com.tesco.pma.error.ErrorCodeAware;

public enum ErrorCodes implements ErrorCodeAware {

    /**
     * Invalid payload
     */
    INVALID_PAYLOAD,

    /**
     * Failed to upload file
     */
    ERROR_FILE_UPLOAD_FAILED,

    /**
     * Failed to register file
     */
    ERROR_FILE_REGISTRATION_FAILED,

    /**
     * Count of data files and metadata files do not match
     */
    FILES_COUNT_MISMATCH;

    @Override
    public String getCode() {
        return name();
    }

}
