package com.tesco.pma.config.parser;

import com.tesco.pma.error.ErrorCodeAware;

public enum ParsingErrorCode implements ErrorCodeAware {
    /**
     * File is not Office Open XML (.xlsx) format
     */
    PARSE_NOT_OOXML,
    /**
     * File has password protection
     */
    PARSE_PASSWORD_PROTECTED,
    /**
     * Error while reading resource
     */
    PARSE_IO,
    /**
     * Error while reading cell
     */
    PARSE_ERROR_CELL,
    /**
     * Unknown cell type
     */
    PARSE_CELL_UNKNOWN_TYPE,
    /**
     * Multi properties in the same line
     */
    PARSE_MULTI_PROPERTIES,
    /**
     * Unexpected error
     */
    PARSE_UNHANDLED_ERROR;

    @Override
    public String getCode() {
        return name();
    }
}
