package com.tesco.pma.organisation.api;

import com.tesco.pma.error.ErrorCodeAware;

public enum ConfigEntryErrorCodes implements ErrorCodeAware {
    /**
     * Config entry not found
     */
    CONFIG_ENTRY_NOT_FOUND,
    /**
     * Config entry already exists
     */
    CONFIG_ENTRY_ALREADY_EXISTS;


    @Override
    public String getCode() {
        return name();
    }
}
