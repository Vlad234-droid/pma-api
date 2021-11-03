package com.tesco.pma.organisation.dao;

import com.tesco.pma.api.GeneralDictionaryItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigEntryTypeDAO {

    /**
     * Find config entry type by its id
     *
     * @param id - identifier
     * @return config entry type object
     */
    GeneralDictionaryItem findConfigEntryType(@Param("id") int id);

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
     * @return number of inserted rows
     */
    int create(@Param("cet") GeneralDictionaryItem item);

    /**
     * Updates config entry type
     *
     * @param item - object to be updated
     * @return number of updated rows
     */
    int update(@Param("cet") GeneralDictionaryItem item);

    /**
     * Deletes config entry type by its id
     *
     * @param id - identifier
     * @return number of deleter rows
     */
    int delete(@Param("id") int id);


}
