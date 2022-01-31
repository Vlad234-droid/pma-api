package com.tesco.pma.organisation.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
    List<ColleagueEntity> findColleaguesByCompositeKey(String compositeKey);

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

    /**
     * Check if colleague exist by composite key
     *
     * @param colleagueUuid - colleague identifier
     * @param key           - composite key
     * @return true/false
     */
    boolean isColleagueExistsForCompositeKey(UUID colleagueUuid, String key);

    void propagateEventsByCompositeKey(String compositeKey, String eventName, Map<String, Serializable> eventParams);

}
