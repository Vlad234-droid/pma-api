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
    ERROR_FILE_UPLOAD_FAILED;

    @Override
    public String getCode() {
        return name();
    }

}
