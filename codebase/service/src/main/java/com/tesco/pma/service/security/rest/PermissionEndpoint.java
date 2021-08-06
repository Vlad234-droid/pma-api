package com.tesco.pma.service.security.rest;

import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.service.security.SubsidiaryPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Permissions endpoint.
 */
@RestController
@RequestMapping("subsidiaries/{subsidiaryUuid}/permissions")
@Validated
@RequiredArgsConstructor
public class PermissionEndpoint {

    private final SubsidiaryPermissionService subsidiaryPermissionService;

    /**
     * Grants subsidiary permission.
     *
     * @param subsidiaryUuid subsidiary identifier
     * @param colleagueUuid  colleague identifier
     * @param role           role name
     * @return success {@link RestResponse} without data.
     */
    @Operation(summary = "Grant permission", description = "Grant subsidiary permission for user with particular role", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Permission granted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Subsidiary not found")
    @ApiResponse(responseCode = HttpStatusCodes.CONFLICT, description = "Permission already exists")
    @PostMapping(value = "colleagues/{colleagueUuid}/roles/{role}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> grantSubsidiaryPermission(@PathVariable UUID subsidiaryUuid,
                                                        @PathVariable UUID colleagueUuid,
                                                        @PathVariable String role) {
        var permission = new SubsidiaryPermission();
        permission.setSubsidiaryUuid(subsidiaryUuid);
        permission.setColleagueUuid(colleagueUuid);
        permission.setRole(role);
        subsidiaryPermissionService.grantSubsidiaryPermission(permission);
        return success();
    }

    /**
     * Revokes subsidiary permission.
     *
     * @param subsidiaryUuid subsidiary identifier
     * @param colleagueUuid  colleague identifier
     * @param role           role name
     * @return success {@link RestResponse} without data.
     */
    @Operation(summary = "Revoke permission", description = "Revoke subsidiary permission for user with particular role", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Permission revoked")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Permission not found")
    @DeleteMapping(value = "colleagues/{colleagueUuid}/roles/{role}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> revokeSubsidiaryPermission(@PathVariable UUID subsidiaryUuid,
                                                         @PathVariable UUID colleagueUuid,
                                                         @PathVariable String role) {
        var permission = new SubsidiaryPermission();
        permission.setSubsidiaryUuid(subsidiaryUuid);
        permission.setColleagueUuid(colleagueUuid);
        permission.setRole(role);
        subsidiaryPermissionService.revokeSubsidiaryPermission(permission);
        return success();
    }

    /**
     * Retrieves subsidiary permissions by subsidiary uuid.
     *
     * @param subsidiaryUuid subsidiary uuid. not null.
     * @return success {@link RestResponse} subsidiary permissions, data could be empty collection.
     */
    @Operation(summary = "Retrieve permissions for subsidiary", description = "Retrieve permissions for subsidiary", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Succeeded")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<Collection<SubsidiaryPermission>> getSubsidiaryPermissions(@PathVariable final UUID subsidiaryUuid) {
        return success(subsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(subsidiaryUuid));
    }
}