package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;

import java.util.UUID;

public interface ConfigEntryService {

    ConfigEntryResponse getStructure(UUID configEntryUuid);

    String generateCompositeKey(UUID configEntryUuid);

    ConfigEntryResponse getPublishedChildStructureByCompositeKey(String key);

    void publishConfigEntry(UUID configEntryUuid);

    void unpublishConfigEntry(UUID configEntryUuid);

    void createConfigEntry(ConfigEntry configEntry);

    void updateConfigEntry(ConfigEntry configEntry);

    void deleteConfigEntry(UUID configEntryUuid);
}
