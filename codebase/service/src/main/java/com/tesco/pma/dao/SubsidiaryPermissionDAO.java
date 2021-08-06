package com.tesco.pma.dao;

import com.tesco.pma.api.security.SubsidiaryPermission;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Interface to perform database operation on {@link SubsidiaryPermission}.
 */
public interface SubsidiaryPermissionDAO {
    /**
     * Create {@link SubsidiaryPermission}.
     *
     * @param permission Subsidiary Permission.
     * @return 1 - succeeded, 0 in case subsidiary doesn't exist.
     * @throws org.springframework.dao.DuplicateKeyException if permission already exists.
     */
    int create(@Param("permission") final SubsidiaryPermission permission);

    /**
     * Deletes {@link SubsidiaryPermission}.
     *
     * @param permission Subsidiary Permission.
     * @return 1 - succeeded, 0 in case subsidiary permission doesn't exist.
     */
    int delete(@Param("permission") final SubsidiaryPermission permission);

    /**
     * Finds {@link SubsidiaryPermission}'s by subsidiary uuid.
     *
     * @param subsidiaryUuid subsidiary uuid. not null.
     * @return subsidiary permissions for subsidiary, never null.
     */
    Set<SubsidiaryPermission> findBySubsidiaryUuid(@Param("subsidiaryUuid") final UUID subsidiaryUuid);

    /**
     * Finds {@link SubsidiaryPermission}'s by user id.
     *
     * @param colleagueUuid colleague uuid. not null.
     * @return subsidiary permissions for user, never null.
     */
    Set<SubsidiaryPermission> findByColleagueUuid(@Param("colleagueUuid") final UUID colleagueUuid);

    /**
     * Finds {@link SubsidiaryPermission}'s by colleague uuids.
     *
     * @param colleagueUuids colleague uuids. not null.
     * @return subsidiary permissions, never null.
     */
    Collection<SubsidiaryPermission> findByColleagueUuids(@Param("colleagueUuids") final Collection<UUID> colleagueUuids);

}
