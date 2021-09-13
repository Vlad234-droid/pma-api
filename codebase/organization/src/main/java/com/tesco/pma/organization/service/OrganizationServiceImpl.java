package com.tesco.pma.organization.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitResponse;
import com.tesco.pma.organization.dao.OrganizationDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private static final String COMPOSITE_KEY_FORMAT = "BusinessUnit/%s/v%d";
    private static final Pattern GET_UUID_COMPOSITE_KEY_PATTERN = Pattern.compile("BusinessUnit/(.+)/v\\d+");
    private final OrganizationDAO dao;

    @Override
    public BusinessUnitResponse getStructure(UUID businessUnitUuid) {
        var parentStructure = dao.findBusinessUnitParentStructure(businessUnitUuid);
        var childStructure = dao.findBusinessUnitChildStructure(businessUnitUuid);

        var set = new HashSet<>(parentStructure);
        set.addAll(childStructure);

        return buildStructure(set);
    }

    @Override
    public String generateCompositeKey(UUID businessUnitUuid) {
        var structureList = dao.findBusinessUnitParentStructure(businessUnitUuid);
        if (structureList.isEmpty()) {
            throw new NotFoundException("CODE", "Not found");
        }
        return structureList.stream()
                .map(bu -> String.format(COMPOSITE_KEY_FORMAT, bu.getUuid(), bu.getVersion()))
                .collect(Collectors.joining(","));
    }

    @Override
    public BusinessUnitResponse getStructureByCompositeKey(String key) {
        var uuidSet = GET_UUID_COMPOSITE_KEY_PATTERN.matcher(key)
                .results()
                .filter(m -> m.groupCount() >= 1)
                .map(m -> m.group(1))
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        var units = dao.findBusinessUnitsByUuids(uuidSet);
        return buildStructure(units);
    }

    @Override
    public void publishUnit(UUID businessUnitUuid) {
        dao.publishBusinessUnit(businessUnitUuid);
    }

    @Override
    public void unpublishUnit(UUID businessUnitUuid) {
        dao.unpublishBusinessUnit(businessUnitUuid);
    }

    @Override
    @Transactional
    public void createBusinessUnit(BusinessUnit businessUnit) {
        var nextVersion = dao.getNextVersion(businessUnit.getName(), businessUnit.getType());
        businessUnit.setUuid(UUID.randomUUID());
        businessUnit.setVersion(nextVersion);
        dao.createBusinessUnit(businessUnit);
    }

    private BusinessUnitResponse buildStructure(Collection<BusinessUnit> units) {
        var root = units.stream()
                .filter(u -> u.getParentUuid() == null)
                .findFirst()
                .map(this::buildResponse)
                .orElseThrow(() -> new NotFoundException("CODE", "no root"));
        units.removeIf(u -> u.getParentUuid() == null);

        var uuidToResponse = units
                .stream()
                .collect(Collectors.toMap(BusinessUnit::getUuid, this::buildResponse));
        uuidToResponse.put(root.getUuid(), root);

        units.forEach(u -> uuidToResponse.get(u.getParentUuid()).getChildren().add(uuidToResponse.get(u.getUuid())));

        return root;
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
