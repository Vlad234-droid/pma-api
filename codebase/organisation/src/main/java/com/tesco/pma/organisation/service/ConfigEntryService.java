package com.tesco.pma.organisation.service;

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
    ConfigEntryResponse getStructure(UUID configEntryUuid);

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
     * @return published child structure includes root object
     */
    ConfigEntryResponse getPublishedChildStructureByCompositeKey(String key);

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

    List<ConfigEntryResponse> getUnpublishedRoots();

    ConfigEntryResponse getUnpublishedChildStructureByCompositeKey(String compositeKey);
}
