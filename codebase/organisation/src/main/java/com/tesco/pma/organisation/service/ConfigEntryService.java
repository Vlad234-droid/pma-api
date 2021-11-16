package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;

import java.util.List;
import java.util.UUID;

public interface ConfigEntryService {

    /**
     * Gets all structure for config entry
     *
     * @param configEntryUuid - config entry identifier
     * @return set of config structure
     */
    ConfigEntryResponse getUnpublishedStructure(UUID configEntryUuid);

    /**
     * Generates composite key for config entry
     *
     * @param configEntryUuid - config entry identifier
     * @return composite key
     */
    String generateCompositeKey(UUID configEntryUuid);

    /**
     * Gets published child structure by composite key
     *
     * @param key - composite key
     * @return published child structure includes root objects
     */
    List<ConfigEntryResponse> getPublishedChildStructureByCompositeKey(String key);

    /**
     * Creates record in publish table for config entry
     *
     * @param configEntryUuid - config entry identifier
     */
    void publishConfigEntry(UUID configEntryUuid);

    /**
     * Unpublish all records for config entry
     *
     * @param configEntryUuid - config entry identifier
     */
    void unpublishConfigEntry(UUID configEntryUuid);

    /**
     * Creates config entry
     *
     * @param configEntry - object to be created
     */
    void createConfigEntry(ConfigEntry configEntry);

    /**
     * Updates config entry
     *
     * @param configEntry - object to be updated
     */
    void updateConfigEntry(ConfigEntry configEntry);

    /**
     * Deletes config entry
     *
     * @param configEntryUuid - config entry identifier
     */
    void deleteConfigEntry(UUID configEntryUuid);

    /**
     * Gets all unpublished root config entries
     *
     * @return list of root entries
     */
    List<ConfigEntryResponse> getUnpublishedRoots();

    /**
     * Gets unpublished child structure by composite key
     *
     * @param compositeKey - composite key
     * @return child structure includes root objects
     */
    List<ConfigEntryResponse> getUnpublishedChildStructureByCompositeKey(String compositeKey);

    /**
     * Gets list of colleagues by composite key
     *
     * @param compositeKey - key
     * @return list of colleagues
     */
    List<Colleague> findColleaguesByCompositeKey(String compositeKey);

    /**
     * Get colleague by iam id
     *
     * @param iamId colleague iam identifier
     * @return colleague object
     */
    Colleague getColleagueByIamId(String iamId);

    /**
     * Get colleague by colleague UUID
     *
     * @param colleagueUuid colleague UUID
     * @return colleague object
     */
    Colleague getColleague(UUID colleagueUuid);

    /**
     * Get all published roots
     *
     * @return list of roots
     */
    List<ConfigEntryResponse> getPublishedRoots();

    /**
     * Get published structure
     *
     * @param entryUuid - root identifier
     * @return config entry structure
     */
    ConfigEntryResponse getPublishedStructure(UUID entryUuid);

}
