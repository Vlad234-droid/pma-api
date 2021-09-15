package com.tesco.pma.organization.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitResponse;
import com.tesco.pma.organization.api.WorkingBusinessUnit;
import com.tesco.pma.organization.dao.OrganizationDAO;
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
public class OrganizationServiceImpl implements OrganizationService {

    private static final String COMPOSITE_KEY_FORMAT = "%s/%s/%s";
    private static final String COMPOSITE_KEY_VERSION_FORMAT = "%s#v%d";
    private final OrganizationDAO dao;

    @Override
    public BusinessUnitResponse getStructure(UUID businessUnitUuid) {
        var fullStructure = dao.getFullStructure(businessUnitUuid);
        return buildStructure(fullStructure);
    }

    @Override
    public String generateCompositeKey(UUID businessUnitUuid) {
        var structureList = dao.findBusinessUnitParentStructure(businessUnitUuid);
        var businessUnit = structureList.stream()
                .filter(bu -> bu.getUuid().equals(businessUnitUuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("CODE", "Not found"));
        return generateCompositeKey(structureList, businessUnit);
    }

    private String generateCompositeKey(Collection<BusinessUnit> structureList, BusinessUnit bu) {
        var key = String.format(COMPOSITE_KEY_FORMAT, bu.getType().getCode(), bu.getName(), StringUtils.EMPTY);
        var map = structureList.stream().collect(Collectors.toMap(BusinessUnit::getUuid, Function.identity()));
        var parentId = bu.getParentUuid();
        while (parentId != null && map.containsKey(parentId)) {
            var parent = map.get(parentId);
            key = String.format(COMPOSITE_KEY_FORMAT, parent.getType().getCode(), parent.getName(), key);
            parentId = parent.getParentUuid();
        }
        return String.format(COMPOSITE_KEY_VERSION_FORMAT, key, bu.getVersion());
    }

    @Override
    public BusinessUnitResponse getPublishedChildStructureByCompositeKey(String key) {
        var versionPosition = key.indexOf("/#v");
        var searchTerm = key.substring(0, versionPosition) + "%" + key.substring(versionPosition);
        var units = dao.findPublishedBusinessUnitsByKey(searchTerm);
        return buildStructure(units);
    }

    @Override
    @Transactional
    public void publishUnit(UUID businessUnitUuid) {
        unpublishUnit(businessUnitUuid);

        var fullStructure = dao.getFullStructure(businessUnitUuid);
        dao.findBusinessUnitChildStructure(businessUnitUuid)
                .stream()
                .map(bu -> {
                    var wbu = new WorkingBusinessUnit();
                    wbu.setName(bu.getName());
                    wbu.setType(bu.getType());
                    wbu.setVersion(bu.getVersion());
                    wbu.setUnitUuid(bu.getUuid());
                    wbu.setCompositeKey(generateCompositeKey(fullStructure, bu));
                    return wbu;
                }).forEach(dao::publishBusinessUnit);
    }

    @Override
    public void unpublishUnit(UUID businessUnitUuid) {
        var compositeKey = generateCompositeKey(businessUnitUuid);
        var searchTerm = compositeKey.replaceAll("/#v\\d+", "%");
        dao.unpublishBusinessUnits(searchTerm);
    }

    @Override
    @Transactional
    public void createBusinessUnit(BusinessUnit businessUnit) {
        businessUnit.setUuid(UUID.randomUUID());
        var parentUuid = businessUnit.getParentUuid();
        if (parentUuid == null) {
            businessUnit.setVersion(1);
            dao.createBusinessUnit(businessUnit);
        } else {
            var root = dao.findRootBusinessUnit(parentUuid);
            var version = root.getVersion() + 1;
            var structure = dao.findBusinessUnitChildStructure(root.getUuid());
            structure.add(businessUnit);
            structure.forEach(bu -> bu.setVersion(version));
            copyStructure(structure);
        }
    }

    @Override
    @Transactional
    public void updateBusinessUnit(BusinessUnit businessUnit) {
        var root = dao.findRootBusinessUnit(businessUnit.getUuid());
        var version = root.getVersion() + 1;
        var structure = dao.findBusinessUnitChildStructure(root.getUuid());
        structure.forEach(bu -> bu.setVersion(version));
        structure.stream()
                .filter(bu -> bu.getUuid().equals(businessUnit.getUuid()))
                .findFirst()
                .map(bu -> {
                    bu.setName(businessUnit.getName());
                    bu.setType(businessUnit.getType());
                    bu.setParentUuid(businessUnit.getParentUuid());
                    return bu;
                }).orElseThrow(() -> new NotFoundException("Code", "not found"));
        copyStructure(structure);
    }

    private void copyStructure(List<BusinessUnit> structure) {
        var oldToNewUuid = new HashMap<UUID, UUID>();
        structure.forEach(bu -> {
            var oldUuid = bu.getUuid();
            bu.setUuid(UUID.randomUUID());
            oldToNewUuid.put(oldUuid, bu.getUuid());
        });
        structure.stream().filter(bu -> bu.getParentUuid() != null)
                .forEach(bu -> bu.setParentUuid(oldToNewUuid.get(bu.getParentUuid())));
        structure.forEach(dao::createBusinessUnit);
    }

    private BusinessUnitResponse buildStructure(Collection<BusinessUnit> units) {
        var uuidToResponse = units
                .stream()
                .collect(Collectors.toMap(BusinessUnit::getUuid, this::buildResponse));

        var rootUuid = units.stream()
                .filter(u -> u.getParentUuid() == null || !uuidToResponse.containsKey(u.getParentUuid()))
                .map(BusinessUnit::getUuid)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("CODE", "no root"));
        units.removeIf(u -> u.getUuid().equals(rootUuid));

        units.forEach(u -> uuidToResponse.get(u.getParentUuid()).getChildren().add(uuidToResponse.get(u.getUuid())));

        return uuidToResponse.get(rootUuid);
    }

    private BusinessUnitResponse buildResponse(BusinessUnit businessUnit) {
        var response = new BusinessUnitResponse();
        response.setUuid(businessUnit.getUuid());
        response.setName(businessUnit.getName());
        response.setType(businessUnit.getType());
        response.setVersion(businessUnit.getVersion());
        response.setRoot(businessUnit.getParentUuid() == null);
        response.setChildren(new ArrayList<>());
        return response;
    }
}
