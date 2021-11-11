package com.tesco.pma.configuration.rest.model;

import com.tesco.pma.api.security.SubsidiaryPermission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Api Model customization for {@link SubsidiaryPermission}.
 *
 * <p>Must reflect validation at 'subsidiary-permission.xml'
 */
@Schema(name = "SubsidiaryPermission")
@SuppressWarnings("PMD.ClassNamingConventions")
public abstract class SubsidiaryPermissionMixIn extends SubsidiaryPermission {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    public abstract UUID getColleagueUuid();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    public abstract UUID getSubsidiaryUuid();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    public abstract String getRole();
}