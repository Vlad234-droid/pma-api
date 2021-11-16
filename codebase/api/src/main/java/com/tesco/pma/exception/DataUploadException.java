package com.tesco.pma.exception;

public class DataUploadException extends AbstractApiRuntimeException {

    public DataUploadException(String code, String message, String source) {
        super(code, message, source);
    }

    public DataUploadException(String code, String message, String source, Throwable cause) {
        super(code, message, source, cause);
    }

}
