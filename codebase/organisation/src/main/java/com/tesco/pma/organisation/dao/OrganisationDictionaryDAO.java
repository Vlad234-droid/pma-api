package com.tesco.pma.organisation.dao;

import com.tesco.pma.organisation.api.OrganisationDictionary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrganisationDictionaryDAO {

    /**
     * Find organisation dictionary by its code
     *
     * @param code - code identifier
     * @return organisation dictionary object
     */
    OrganisationDictionary findOrganisationDictionary(@Param("code") String code);

    /**
     * Find all organisation dictionaries
     *
     * @return list of organisation dictionaries
     */
    List<OrganisationDictionary> findAllOrganisationDictionaries();

    /**
     * Creates organisation dictionary
     *
     * @param item - object to be created
     * @return number of inserted rows
     */
    int create(@Param("od") OrganisationDictionary item);

    /**
     * Updates organisation dictionary
     *
     * @param item - object to be updated
     * @return number of updated rows
     */
    int update(@Param("od") OrganisationDictionary item);

    /**
     * Deletes organisation dictionary by its code
     *
     * @param code - code identifier
     * @return number of deleted rows
     */
    int delete(@Param("code") String code);


}
