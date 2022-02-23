package com.tesco.pma.exception;

/**
 * Exception for write errors
 */
public class WriteException extends AbstractApiRuntimeException {

    public WriteException(String code, String message, String source) {
        super(code, message, source);
    }

    public WriteException(String code, String message, String source, Throwable cause) {
        super(code, message, source, cause);
    }
}