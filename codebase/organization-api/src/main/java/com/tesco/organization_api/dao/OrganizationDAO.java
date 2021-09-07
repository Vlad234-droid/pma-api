package com.tesco.organization_api.dao;

import com.tesco.organization_api.api.ColleagueOrganizationTree;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

public interface OrganizationDAO {
    boolean isColleagueSynced(@Param("colleagueUuid") UUID colleagueUuid);

    ColleagueOrganizationTree getColleagueTree(@Param("colleagueUuid") UUID colleagueUuid, @Param("departmentId") String departmentId);

    boolean isDepartmentPresent(@Param("id") String id);

    int saveDepartment(@Param("department") Object department);

    int saveColleague(@Param("colleagueUuid") UUID colleagueUuid, @Param("synced") boolean synced);

    int saveWorkRelationship(@Param("colleagueUuid") UUID colleagueUuid,
                             @Param("managerUuid") UUID managerUuid,
                             @Param("departmentId") String departmentId);

    int deleteWorkRelationshipByUuid(@Param("uuid") UUID uuid);

    int deleteWorkRelationship(@Param("colleagueUuid") UUID colleagueUuid,
                               @Param("managerUuid") UUID managerUuid,
                               @Param("departmentId") String departmentId);
}
