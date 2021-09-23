package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.BusinessUnit;
import com.tesco.pma.organisation.api.BusinessUnitResponse;

import java.util.UUID;

public interface OrganisationService {

    BusinessUnitResponse getStructure(UUID businessUnitUuid);

    String generateCompositeKey(UUID businessUnitUuid);

    BusinessUnitResponse getPublishedChildStructureByCompositeKey(String key);

    void publishUnit(UUID businessUnitUuid);

    void unpublishUnit(UUID businessUnitUuid);

    void createBusinessUnit(BusinessUnit businessUnit);

    void updateBusinessUnit(BusinessUnit businessUnit);

    void deleteBusinessUnit(UUID buUuid);
}
