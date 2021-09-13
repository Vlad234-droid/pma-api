package com.tesco.pma.organization.service;

import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitResponse;

import java.util.UUID;

public interface OrganizationService {

    BusinessUnitResponse getStructure(UUID businessUnitUuid);

    String generateCompositeKey(UUID businessUnitUuid);

    BusinessUnitResponse getStructureByCompositeKey(String key);

    void publishUnit(UUID businessUnitUuid);

    void unpublishUnit(UUID businessUnitUuid);

    void createBusinessUnit(BusinessUnit businessUnit);
}
