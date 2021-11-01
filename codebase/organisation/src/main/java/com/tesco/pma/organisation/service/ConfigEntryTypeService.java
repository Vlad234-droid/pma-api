package com.tesco.pma.organisation.service;

import com.tesco.pma.api.GeneralDictionaryItem;

import java.util.List;

public interface ConfigEntryTypeService {

    /**
     * Find config entry type by its id
     *
     * @param id - identifier
     * @return config entry type object
     */
    GeneralDictionaryItem findConfigEntryType(int id);

    /**
     * Find all config entry types
     *
     * @return list of config entry types
     */
    List<GeneralDictionaryItem> findAllConfigEntryTypes();

    /**
     * Creates config entry type
     *
     * @param item - object to be created
     * @return created object
     */
    GeneralDictionaryItem create(GeneralDictionaryItem item);

    /**
     * Updates config entry type
     *
     * @param item - object to be updated
     * @return updated object
     */
    GeneralDictionaryItem update(GeneralDictionaryItem item);

    /**
     * Deletes config entry type by its identifier
     *
     * @param id - identifier
     */
    void delete(int id);

}
