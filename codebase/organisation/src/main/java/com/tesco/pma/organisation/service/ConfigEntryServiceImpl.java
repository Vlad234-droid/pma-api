package com.tesco.pma.organisation.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryErrorCodes;
import com.tesco.pma.organisation.api.ConfigEntryResponse;
import com.tesco.pma.organisation.api.WorkingConfigEntry;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.organisation.dao.ConfigEntryTypeDAO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ConfigEntryServiceImpl implements ConfigEntryService {

    private static final String COMPOSITE_KEY_FORMAT = "%s/%s/%s";
    private static final String COMPOSITE_KEY_VERSION_FORMAT = "%s#v%d";
    private static final String ID = "id";
    private final ConfigEntryDAO dao;
    private final ConfigEntryTypeDAO configEntryTypeDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public ConfigEntryResponse getUnpublishedStructure(UUID configEntryUuid) {
        var fullStructure = dao.getFullStructure(configEntryUuid);
        return CollectionUtils.firstElement(buildStructure(fullStructure));
    }

    @Override
    public String generateCompositeKey(UUID configEntryUuid) {
        var structureList = dao.findConfigEntryParentStructure(configEntryUuid);
        var configEntry = structureList.stream()
                .filter(ce -> ce.getUuid().equals(configEntryUuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ConfigEntryErrorCodes.CONFIG_ENTRY_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_NOT_FOUND, Map.of(ID, configEntryUuid))));
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
    public List<ConfigEntryResponse> getPublishedChildStructureByCompositeKey(String key) {
        String searchTerm = buildCompositeKeySearchTerm(key);
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
        configEntry.setType(configEntryTypeDAO.findConfigEntryType(configEntry.getType().getId()));
        var parentUuid = configEntry.getParentUuid();
        if (parentUuid == null) {
            configEntry.setVersion(1);
            configEntry.setCompositeKey(generateCompositeKey(Collections.emptySet(), configEntry));
            try {
                dao.createConfigEntry(configEntry);
            } catch (DuplicateKeyException ex) {
                throw new DatabaseConstraintViolationException(ConfigEntryErrorCodes.CONFIG_ENTRY_ALREADY_EXISTS.getCode(),
                        messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_ALREADY_EXISTS), null, ex);
            }
        } else {
            var root = dao.findRootConfigEntry(parentUuid);
            var version = dao.getMaxVersionForRootEntry(root.getName(), root.getType().getId()) + 1;
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
        var version = dao.getMaxVersionForRootEntry(root.getName(), root.getType().getId()) + 1;
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
                }).orElseThrow(() -> new NotFoundException(ConfigEntryErrorCodes.CONFIG_ENTRY_NOT_FOUND.getCode(),
                messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_NOT_FOUND, Map.of(ID, configEntry.getUuid()))));
        copyStructure(structure);
    }

    @Override
    @Transactional
    public void deleteConfigEntry(UUID configEntryUuid) {
        var root = dao.findRootConfigEntry(configEntryUuid);
        if (root.getUuid().equals(configEntryUuid)) {
            dao.deleteConfigEntry(configEntryUuid);
        }
        var version = dao.getMaxVersionForRootEntry(root.getName(), root.getType().getId()) + 1;
        var structure = dao.findConfigEntryChildStructure(root.getUuid());
        structure = structure.stream().takeWhile(ce -> !ce.getUuid().equals(configEntryUuid)).collect(Collectors.toList());
        structure.forEach(ce -> ce.setVersion(version));
        copyStructure(structure);
    }

    @Override
    public List<ConfigEntryResponse> getUnpublishedRoots() {
        return dao.findAllUnpublishedRootEntries()
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigEntryResponse> getPublishedRoots() {
        return dao.findAllPublishedRootEntries()
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConfigEntryResponse getPublishedStructure(UUID entryUuid) {
        var structure = dao.findPublishedConfigEntryChildStructure(entryUuid);
        return CollectionUtils.firstElement(buildStructure(structure));
    }

    @Override
    public boolean isColleagueExistsForCompositeKey(UUID colleagueUuid, String compositeKey) {
        String searchKey = getSearchKey(compositeKey);
        return dao.isColleagueExistsForCompositeKey(colleagueUuid, searchKey);
    }

    @Override
    public List<ConfigEntryResponse> getUnpublishedChildStructureByCompositeKey(String compositeKey) {
        String searchTerm = buildCompositeKeySearchTerm(compositeKey);
        var entries = dao.findConfigEntriesByKey(searchTerm);
        return buildStructure(entries);
    }

    @Override
    public List<ColleagueEntity> findColleaguesByCompositeKey(String compositeKey) {
        String searchKey = getSearchKey(compositeKey);
        return dao.findColleaguesByCompositeKey(searchKey);
    }

    @Override
    public List<ColleagueEntity> findColleaguesByCompositeKeyAndHireDate(String compositeKey, LocalDate hireDate) {
        String searchKey = getSearchKey(compositeKey);
        return dao.findColleaguesByCompositeKey(searchKey, hireDate);
    }

    private String getSearchKey(String compositeKey) {
        var parts = compositeKey.split("/");
        return IntStream.range(0, parts.length)
                .filter(i -> i % 2 == 1).mapToObj(i -> parts[i]).collect(Collectors.joining("/"));
    }

    private String buildCompositeKeySearchTerm(String key) {
        if (key.contains("/#v")) {
            var versionPosition = key.indexOf("/#v");
            return key.substring(0, versionPosition) + "%" + key.substring(versionPosition);
        }
        return key + "%";
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
        structure.forEach(ce -> ce.setCompositeKey(generateCompositeKey(structure, ce)));
        try {
            structure.forEach(dao::createConfigEntry);
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(ConfigEntryErrorCodes.CONFIG_ENTRY_ALREADY_EXISTS.getCode(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_ALREADY_EXISTS), null, ex);
        }
    }

    private List<ConfigEntryResponse> buildStructure(Collection<ConfigEntry> entries) {
        var uuidToResponse = entries
                .stream()
                .collect(Collectors.toMap(ConfigEntry::getUuid, this::buildResponse));

        var rootUuids = entries.stream()
                .filter(entry -> entry.getParentUuid() == null || !uuidToResponse.containsKey(entry.getParentUuid()))
                .map(ConfigEntry::getUuid)
                .collect(Collectors.toList());
        entries.removeIf(entry -> rootUuids.contains(entry.getUuid()));

        entries.forEach(entry -> uuidToResponse.get(entry.getParentUuid()).getChildren().add(uuidToResponse.get(entry.getUuid())));

        return uuidToResponse.entrySet().stream()
                .filter(entry -> rootUuids.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private ConfigEntryResponse buildResponse(ConfigEntry configEntry) {
        var response = new ConfigEntryResponse();
        response.setUuid(configEntry.getUuid());
        response.setName(configEntry.getName());
        response.setType(configEntry.getType());
        response.setVersion(configEntry.getVersion());
        response.setRoot(configEntry.getParentUuid() == null);
        response.setCompositeKey(configEntry.getCompositeKey());
        response.setChildren(new ArrayList<>());
        return response;
    }
}
