package com.tesco.pma.organization.dao;

import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitType;
import com.tesco.pma.organization.api.WorkingBusinessUnit;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/*
1. Когда увеличиваем версию.
Апдейт+
 */
public interface OrganizationDAO {

    BusinessUnit findRootBusinessUnit(@Param("uuid") UUID uuid);

    List<BusinessUnit> findBusinessUnitParentStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findBusinessUnitChildStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findPublishedBusinessUnitsByKey(@Param("key") String key);

    int createBusinessUnit(@Param("bu") BusinessUnit businessUnit);

    void publishBusinessUnit(@Param("wbu") WorkingBusinessUnit workingBusinessUnit);

    void unpublishBusinessUnits(@Param("key") String key);

    default Set<BusinessUnit> getFullStructure(UUID businessUnitUuid) {
        var parentStructure = findBusinessUnitParentStructure(businessUnitUuid);
        var childStructure = findBusinessUnitChildStructure(businessUnitUuid);

        var set = new HashSet<>(parentStructure);
        set.addAll(childStructure);
        return set;
    }

}
