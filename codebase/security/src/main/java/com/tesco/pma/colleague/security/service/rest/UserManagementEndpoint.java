package com.tesco.pma.colleague.security.service.rest;

import com.tesco.pma.colleague.security.domain.*;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 *
 * <p>For more information:
 *  @see <a href="https://github.dev.global.tesco.org/97-TeamTools/Colleague-Authentication-and-Access/wiki/REST-Provisioning-APIs">here</a>
 *
 */
@RestController
@RequestMapping(path = "/security", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UserManagementEndpoint {

    private final UserManagementService userManagementService;

    @Operation(summary = "Get users, their status and access levels", description = "Get users, their status and access levels", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Users, their status and access levels found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Users, their status and access levels found not found")
    @GetMapping(value = "/user-management/accounts")
    public RestResponse<List<Account>> getAccounts() {
        return RestResponse.success(userManagementService.getAccounts());
    }

    @Operation(summary = "Create an Account", description = "Create an Account", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/user-management/accounts", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Void> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        userManagementService.createAccount(request);
        return RestResponse.success();
    }

    @Operation(summary = "Disable an account", description = "Disable an account", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PatchMapping(path = "/user-management/accounts", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Void> disableAccount(@RequestBody @Valid DisableAccountRequest request) {
        userManagementService.disableAccount(request);
        return RestResponse.success();
    }

    @Operation(summary = "Enable an account", description = "Enable an account", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PutMapping(path = "/user-management/accounts", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Void> enableAccount(@RequestBody @Valid EnableAccountRequest request) {
        userManagementService.enableAccount(request);
        return RestResponse.success();
    }

    @Operation(summary = "Get available access levels & metadata", description = "Available access levels & metadata", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Available access levels & metadata found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Available access levels & metadata not found")
    @GetMapping(value = "/role-management/roles")
    public RestResponse<List<Role>> getRoles() {
        return RestResponse.success(userManagementService.getRoles());
    }

    @Operation(summary = "Add access to an account", description = "Add access to an account", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/role-management/roles", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Void> grantRole(@RequestBody @Valid AssignRoleRequest request) {
        userManagementService.grantRole(request);
        return RestResponse.success();
    }

    @Operation(summary = "Remove access from an account", description = "Remove access from an account", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @DeleteMapping(path = "/role-management/roles", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Void> revokeRole(@RequestBody @Valid RemoveRoleRequest request) {
        userManagementService.revokeRole(request);
        return RestResponse.success();
    }

}
