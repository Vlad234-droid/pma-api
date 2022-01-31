package com.tesco.pma.exception;

/**
 * Exception for download errors
 */
public class DownloadException extends AbstractApiRuntimeException {

    public DownloadException(String code, String message, String source) {
        super(code, message, source);
    }

    public DownloadException(String code, String message, String source, Throwable cause) {
        super(code, message, source, cause);
    }
}