package com.tesco.pma.organisation.dao;

import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ConfigEntryDAO {

    /**
     * Find root config entry by child uuid
     *
     * @param uuid - child uuid
     * @return root config entry object
     */
    ConfigEntry findRootConfigEntry(@Param("uuid") UUID uuid);

    /**
     * Get all parent structure for config entry
     *
     * @param uuid - child uuid
     * @return parent structure includes child object
     */
    List<ConfigEntry> findConfigEntryParentStructure(@Param("uuid") UUID uuid);

    /**
     * Get all child structure for config entry
     *
     * @param uuid - root uuid
     * @return parent structure includes root object
     */
    List<ConfigEntry> findConfigEntryChildStructure(@Param("uuid") UUID uuid);

    /**
     * Gets published child structure by composite key
     *
     * @param key - composite key
     * @return published child structure includes root object
     */
    List<ConfigEntry> findPublishedConfigEntriesByKey(@Param("key") String key);

    /**
     * Gets unpublished child structure by composite key
     *
     * @param key - composite key
     * @return child structure includes root object
     */
    List<ConfigEntry> findConfigEntriesByKey(@Param("key") String key);

    /**
     * Gets all unpublished root config entries
     *
     * @return list of root entries
     */
    List<ConfigEntry> findAllRootEntries();

    /**
     * Creates config entry
     *
     * @param configEntry - object to be created
     * @return 1 is save success, 0 if failed
     */
    int createConfigEntry(@Param("ce") ConfigEntry configEntry);

    /**
     * Gets max version by name and type. If nothing found returns 0
     *
     * @param name   - entry name
     * @param typeId - type identifier
     * @return max version or 0 if nothing found
     */
    int getMaxVersionForRootEntry(@Param("name") String name, @Param("typeId") int typeId);

    /**
     * Creates record in publish table for config entry
     *
     * @param workingConfigEntry - config entry
     */
    void publishConfigEntry(@Param("wce") WorkingConfigEntry workingConfigEntry);

    /**
     * Deletes all rows from publish table by composite key
     *
     * @param key - composite key
     */
    void unpublishConfigEntries(@Param("key") String key);

    /**
     * Find config entry type by its id
     *
     * @param id - identifier
     * @return config entry type object
     */
    GeneralDictionaryItem findConfigEntryType(@Param("id") int id);

    /**
     * Delete unpublished config entry by its uuid
     *
     * @param uuid - entry identifier
     */
    void deleteConfigEntry(@Param("uuid") UUID uuid);

    /**
     * Gets all structure for config entry
     *
     * @param configEntryUuid - config entry identifier
     * @return set of config structure
     */
    default Set<ConfigEntry> getFullStructure(UUID configEntryUuid) {
        var parentStructure = findConfigEntryParentStructure(configEntryUuid);
        var childStructure = findConfigEntryChildStructure(configEntryUuid);

        var set = new HashSet<>(parentStructure);
        set.addAll(childStructure);
        return set;
    }

}
