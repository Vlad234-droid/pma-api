package com.tesco.pma.service.security;

import com.tesco.pma.api.security.SubsidiaryPermission;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Subsidiary permission service interface.
 * Implementation classes must be annotated with {@link org.springframework.validation.annotation.Validated}.
 */
public interface SubsidiaryPermissionService {
    /**
     * Grants subsidiary permission.
     *
     * @param permission subsidiary permission, not null.
     * @throws NotFoundException      in case subsidiary not found.
     * @throws AlreadyExistsException in case subsidiary permission already exists.
     */
    void grantSubsidiaryPermission(@NotNull @Valid SubsidiaryPermission permission);

    /**
     * Revokes subsidiary permission.
     *
     * @param permission subsidiary permission, not null.
     * @throws NotFoundException in case subsidiary permission not found.
     */
    void revokeSubsidiaryPermission(@NotNull @Valid SubsidiaryPermission permission);

    /**
     * Finds subsidiary permissions by subsidiary uuid.
     *
     * @param subsidiaryUuid subsidiary uuid. not null.
     * @return subsidiary permissions, never null.
     */
    Collection<SubsidiaryPermission> findSubsidiaryPermissionsForSubsidiary(@NotNull UUID subsidiaryUuid);

    /**
     * Finds subsidiary permissions by user id.
     *
     * @param colleagueUuid colleague uuid. not null.
     * @return subsidiary permissions, never null.
     */
    Collection<SubsidiaryPermission> findSubsidiaryPermissionsForUser(@NotNull UUID colleagueUuid);

    /**
     * Finds subsidiary permissions by colleague uuids.
     *
     * @param colleagueUuids colleague uuids.
     * @return subsidiary permissions by colleague uuid, never null.
     */
    Map<UUID, Collection<SubsidiaryPermission>> findSubsidiaryPermissionsForUsers(Collection<UUID> colleagueUuids);
}