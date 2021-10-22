package com.tesco.pma.colleague.security.service.rest;

import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/security", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UserManagementEndpoint {

    private final UserManagementService userManagementService;

    @Operation(summary = "Get available access levels & metadata", description = "Available access levels & metadata", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Available access levels & metadata found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Available access levels & metadata not found")
    @GetMapping(value = "/role-management", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Role>> getRoles() {
        return RestResponse.success(userManagementService.getRoles());
    }

    @Operation(summary = "Get users, their status and access levels", description = "Get users, their status and access levels", tags = "security")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Users, their status and access levels found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Users, their status and access levels found not found")
    @GetMapping(value = "/user-management", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Account>> getAccounts() {
        return RestResponse.success(userManagementService.getAccounts());
    }

}
