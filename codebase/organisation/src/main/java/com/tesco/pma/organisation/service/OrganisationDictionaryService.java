package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.OrganisationDictionary;

import java.util.List;

public interface OrganisationDictionaryService {

    /**
     * Find organisation dictionary by its code
     *
     * @param code - code identifier
     * @return organisation dictionary object
     */
    OrganisationDictionary findOrganisationDictionary(String code);

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
     * @return created object
     */
    OrganisationDictionary create(OrganisationDictionary item);

    /**
     * Updates organisation dictionary
     *
     * @param item - object to be updated
     * @return updated object
     */
    OrganisationDictionary update(OrganisationDictionary item);

    /**
     * Deletes organisation dictionary by its code
     *
     * @param code - code identifier
     */
    void delete(String code);

}
