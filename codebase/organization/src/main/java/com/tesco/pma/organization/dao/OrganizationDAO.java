package com.tesco.pma.organization.dao;

import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitType;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrganizationDAO {

    List<BusinessUnit> findBusinessUnitParentStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findBusinessUnitChildStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findBusinessUnitsByUuids(@Param("uuids") Collection<UUID> uuid);

    int getNextVersion(@Param("name") String name, @Param("type") BusinessUnitType type);

    int createBusinessUnit(@Param("bu") BusinessUnit businessUnit);

    void publishBusinessUnit(@Param("uuid") UUID uuid);

    void unpublishBusinessUnit(@Param("uuid") UUID uuid);

}
