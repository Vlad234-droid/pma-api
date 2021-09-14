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
Создание +
Апдейт+
Как увеличивать+
Копировать все дерево?+

2. Ключ формат
BU/Tesco/BU/Auchan/#v1

3.
Когда мы должны читать именно с воркинг таблицы
 */
public interface OrganizationDAO {

    List<BusinessUnit> findBusinessUnitParentStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findBusinessUnitChildStructure(@Param("uuid") UUID uuid);

    List<BusinessUnit> findPublishedBusinessUnitsByKey(@Param("key") String key);

    int getCurrentPublishedVersion(@Param("key") String key);

    int getNextUnpublishedVersion(@Param("name") String name, @Param("type") BusinessUnitType type);

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
