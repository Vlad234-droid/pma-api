package com.tesco.pma.colleague.security.service.rest;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.RoleRequest;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.colleague.security.service.rest.response.RestResponseWrapper;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * For more information:
 * @see <a href="https://github.dev.global.tesco.org/97-TeamTools/Colleague-Authentication-and-Access/wiki/REST-Provisioning-APIs">here</a>
 *
 */
@RestController
@RequestMapping(path = "/user-management", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UserManagementEndpoint {

    private final UserManagementService userManagementService;

    @Operation(summary = "Get users, their status and access levels", description = "Get users, their status and access levels",
            tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Users, their status and access levels found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Users, their status and access levels found not found")
    @GetMapping(path = "/accounts")
    @PreAuthorize("isAdmin()")
    public RestResponseWrapper<List<Account>> getAccounts(@RequestParam(required = false, defaultValue = "1") int nextPageToken) {
        List<Account> accounts = userManagementService.getAccounts(nextPageToken);
        int nextPage = userManagementService.getNextPageToken(nextPageToken, accounts.size());
        return new RestResponseWrapper<>(RestResponse.success(accounts), nextPage);
    }

    @Operation(summary = "Create an Account", description = "Create an Account", tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/accounts", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<Void> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        userManagementService.createAccount(request);
        return RestResponse.success();
    }

    @Operation(summary = "Enable / Disable an account", description = "Enable / Disable an account", tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PutMapping(path = "/accounts", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<Void> changeAccountStatus(@RequestBody @Valid ChangeAccountStatusRequest request) {
        userManagementService.changeAccountStatus(request);
        return RestResponse.success();
    }

    @Operation(summary = "Get available access levels & metadata", description = "Available access levels & metadata",
            tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Available access levels & metadata found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Available access levels & metadata not found")
    @GetMapping(value = "/roles")
    @PreAuthorize("isAdmin()")
    public RestResponse<List<Role>> getRoles() {
        return RestResponse.success(userManagementService.getRoles());
    }

    @Operation(summary = "Add access to an account", description = "Add access to an account", tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/roles", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<Void> grantRole(@RequestBody @Valid RoleRequest request) {
        userManagementService.grantRole(request);
        return RestResponse.success();
    }

    @Operation(summary = "Remove access from an account", description = "Remove access from an account", tags = "user-management")
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @DeleteMapping(path = "/roles", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<Void> revokeRole(@RequestBody @Valid RoleRequest request) {
        userManagementService.revokeRole(request);
        return RestResponse.success();
    }

}
