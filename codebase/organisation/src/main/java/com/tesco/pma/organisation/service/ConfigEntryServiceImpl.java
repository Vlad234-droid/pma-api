package com.tesco.pma.organisation.service;

import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigEntryServiceImpl implements ConfigEntryService {

    private static final String COMPOSITE_KEY_FORMAT = "%s/%s/%s";
    private static final String COMPOSITE_KEY_VERSION_FORMAT = "%s#v%d";
    private final ConfigEntryDAO dao;

    @Override
    public ConfigEntryResponse getStructure(UUID configEntryUuid) {
        var fullStructure = dao.getFullStructure(configEntryUuid);
        return buildStructure(fullStructure);
    }

    @Override
    public String generateCompositeKey(UUID configEntryUuid) {
        var structureList = dao.findConfigEntryParentStructure(configEntryUuid);
        var configEntry = structureList.stream()
                .filter(ce -> ce.getUuid().equals(configEntryUuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCodes.CONFIG_ENTRY_NOT_FOUND.getCode(), "Config entry not found by id"));
        return generateCompositeKey(structureList, configEntry);
    }

    private String generateCompositeKey(Collection<ConfigEntry> structureList, ConfigEntry configEntry) {
        var key = String.format(COMPOSITE_KEY_FORMAT, configEntry.getType().getCode(), configEntry.getName(), StringUtils.EMPTY);
        var map = structureList.stream().collect(Collectors.toMap(ConfigEntry::getUuid, Function.identity()));
        var parentId = configEntry.getParentUuid();
        while (parentId != null && map.containsKey(parentId)) {
            var parent = map.get(parentId);
            key = String.format(COMPOSITE_KEY_FORMAT, parent.getType().getCode(), parent.getName(), key);
            parentId = parent.getParentUuid();
        }
        return String.format(COMPOSITE_KEY_VERSION_FORMAT, key, configEntry.getVersion());
    }

    @Override
    public ConfigEntryResponse getPublishedChildStructureByCompositeKey(String key) {
        var versionPosition = key.indexOf("/#v");
        var searchTerm = key.substring(0, versionPosition) + "%" + key.substring(versionPosition);
        var entries = dao.findPublishedConfigEntriesByKey(searchTerm);
        return buildStructure(entries);
    }

    @Override
    @Transactional
    public void publishConfigEntry(UUID configEntryUuid) {
        unpublishConfigEntry(configEntryUuid);

        var fullStructure = dao.getFullStructure(configEntryUuid);
        dao.findConfigEntryChildStructure(configEntryUuid)
                .stream()
                .map(ce -> {
                    var wce = new WorkingConfigEntry();
                    wce.setName(ce.getName());
                    wce.setType(ce.getType());
                    wce.setVersion(ce.getVersion());
                    wce.setConfigEntryUuid(ce.getUuid());
                    wce.setCompositeKey(generateCompositeKey(fullStructure, ce));
                    return wce;
                }).forEach(dao::publishConfigEntry);
    }

    @Override
    public void unpublishConfigEntry(UUID configEntryUuid) {
        var compositeKey = generateCompositeKey(configEntryUuid);
        var searchTerm = compositeKey.replaceAll("/#v\\d+", "%");
        dao.unpublishConfigEntries(searchTerm);
    }

    @Override
    @Transactional
    public void createConfigEntry(ConfigEntry configEntry) {
        configEntry.setUuid(UUID.randomUUID());
        var parentUuid = configEntry.getParentUuid();
        if (parentUuid == null) {
            configEntry.setVersion(1);
            dao.createConfigEntry(configEntry);
        } else {
            var root = dao.findRootConfigEntry(parentUuid);
            var version = root.getVersion() + 1;
            var structure = dao.findConfigEntryChildStructure(root.getUuid());
            structure.add(configEntry);
            structure.forEach(bu -> bu.setVersion(version));
            copyStructure(structure);
        }
    }

    @Override
    @Transactional
    public void updateConfigEntry(ConfigEntry configEntry) {
        var root = dao.findRootConfigEntry(configEntry.getUuid());
        var version = root.getVersion() + 1;
        var structure = dao.findConfigEntryChildStructure(root.getUuid());
        structure.forEach(bu -> bu.setVersion(version));
        structure.stream()
                .filter(ce -> ce.getUuid().equals(configEntry.getUuid()))
                .findFirst()
                .map(ce -> {
                    ce.setName(configEntry.getName());
                    ce.setType(configEntry.getType());
                    ce.setParentUuid(configEntry.getParentUuid());
                    return ce;
                }).orElseThrow(() -> new NotFoundException(ErrorCodes.CONFIG_ENTRY_NOT_FOUND.getCode(), "Config entry not found by id"));
        copyStructure(structure);
    }

    @Override
    @Transactional
    public void deleteConfigEntry(UUID configEntryUuid) {
        var root = dao.findRootConfigEntry(configEntryUuid);
        var version = root.getVersion() + 1;
        var structure = dao.findConfigEntryChildStructure(root.getUuid());
        structure = structure.stream().takeWhile(ce -> !ce.getUuid().equals(configEntryUuid)).collect(Collectors.toList());
        structure.forEach(ce -> ce.setVersion(version));
        copyStructure(structure);
    }

    private void copyStructure(List<ConfigEntry> structure) {
        var oldToNewUuid = new HashMap<UUID, UUID>();
        structure.forEach(ce -> {
            var oldUuid = ce.getUuid();
            ce.setUuid(UUID.randomUUID());
            oldToNewUuid.put(oldUuid, ce.getUuid());
        });
        structure.stream().filter(ce -> ce.getParentUuid() != null)
                .forEach(ce -> ce.setParentUuid(oldToNewUuid.get(ce.getParentUuid())));
        structure.forEach(dao::createConfigEntry);
    }

    private ConfigEntryResponse buildStructure(Collection<ConfigEntry> entries) {
        var uuidToResponse = entries
                .stream()
                .collect(Collectors.toMap(ConfigEntry::getUuid, this::buildResponse));

        var rootUuid = entries.stream()
                .filter(entry -> entry.getParentUuid() == null || !uuidToResponse.containsKey(entry.getParentUuid()))
                .map(ConfigEntry::getUuid)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCodes.CONFIG_ENTRY_NOT_FOUND.getCode(), "Cannot find root config entry"));
        entries.removeIf(entry -> entry.getUuid().equals(rootUuid));

        entries.forEach(entry -> uuidToResponse.get(entry.getParentUuid()).getChildren().add(uuidToResponse.get(entry.getUuid())));

        return uuidToResponse.get(rootUuid);
    }

    private ConfigEntryResponse buildResponse(ConfigEntry configEntry) {
        var response = new ConfigEntryResponse();
        response.setUuid(configEntry.getUuid());
        response.setName(configEntry.getName());
        response.setType(configEntry.getType());
        response.setVersion(configEntry.getVersion());
        response.setRoot(configEntry.getParentUuid() == null);
        response.setChildren(new ArrayList<>());
        return response;
    }
}
