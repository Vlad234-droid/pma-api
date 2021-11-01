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
    CONFIG_ENTRY_ALREADY_EXISTS,
    /**
     * Config entry tyoe not found
     */
    CONFIG_ENTRY_TYPE_NOT_FOUND,
    /**
     * Config entry type already exists
     */
    CONFIG_ENTRY_TYPE_ALREADY_EXISTS,
    /**
     * Organisation dictionary not found
     */
    ORGANISATION_DICTIONARY_NOT_FOUND,
    /**
     * Organisation dictionary already exists
     */
    ORGANISATION_DICTIONARY_ALREADY_EXISTS;


    @Override
    public String getCode() {
        return name();
    }
}
