package com.tesco.pma.organisation.dao;

import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/*
1. Когда увеличиваем версию.
Апдейт+
 */
public interface ConfigEntryDAO {

    ConfigEntry findRootConfigEntry(@Param("uuid") UUID uuid);

    List<ConfigEntry> findConfigEntryParentStructure(@Param("uuid") UUID uuid);

    List<ConfigEntry> findConfigEntryChildStructure(@Param("uuid") UUID uuid);

    List<ConfigEntry> findPublishedConfigEntriesByKey(@Param("key") String key);

    int createConfigEntry(@Param("ce") ConfigEntry configEntry);

    void publishConfigEntry(@Param("wce") WorkingConfigEntry workingConfigEntry);

    void unpublishConfigEntries(@Param("key") String key);

    default Set<ConfigEntry> getFullStructure(UUID businessUnitUuid) {
        var parentStructure = findConfigEntryParentStructure(businessUnitUuid);
        var childStructure = findConfigEntryChildStructure(businessUnitUuid);

        var set = new HashSet<>(parentStructure);
        set.addAll(childStructure);
        return set;
    }

}
