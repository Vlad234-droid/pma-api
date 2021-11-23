package com.tesco.pma.user.rest;

import com.tesco.pma.api.User;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.USER_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/users", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class UserEndpoint {
    private final UserService userService;
    private final NamedMessageSourceAccessor messages;

    @Operation(summary = "Get user by colleague uuid", description = "Get user by colleague uuid", tags = "user")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "User found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "User not found")
    @GetMapping(path = "/{colleagueUuid}")
    public RestResponse<User> getUserByColleagueUuid(@PathVariable UUID colleagueUuid) {
        return RestResponse.success(userService.findUserByColleagueUuid(colleagueUuid)
                .orElseThrow(() -> notFound("colleagueUuid", colleagueUuid)));
    }

    @Operation(summary = "Get user by iam id (TPX)", description = "Get user by iam id (TPX)", tags = "user")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "User found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "User not found")
    @GetMapping(path = "/iam-ids/{iamId}")
    public RestResponse<User> getUserByIamId(@PathVariable String iamId) {
        return RestResponse.success(userService.findUserByIamId(iamId)
                .orElseThrow(() -> notFound("iamId", iamId)));
    }

    @Operation(summary = "Get me", description = "Get user info for current authenticated user", tags = "user")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "User found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "User not found")
    @GetMapping(path = "/me")
    @PreAuthorize("hasAnyRole()")
    public RestResponse<User> getMe() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userOptional = userService.findUserByAuthentication(authentication);
        if (userOptional.isEmpty()) {
            throw notFound("authentication.name", authentication.getName());
        }

        return RestResponse.success(userOptional.get());
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(USER_NOT_FOUND.getCode(), messages.getMessage(USER_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }
}
