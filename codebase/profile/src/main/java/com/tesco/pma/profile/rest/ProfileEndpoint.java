package com.tesco.pma.profile.rest;

import com.tesco.pma.api.Profile;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.profile.service.ProfileService;
import com.tesco.pma.service.user.UserIncludes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.USER_NOT_FOUND;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/profile", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class ProfileEndpoint {

    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messages;

    @Operation(summary = "Get profile by colleague uuid", description = "Get profile by colleague uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found")
    @GetMapping(path = "/{colleagueUuid}")
    public RestResponse<Profile> getProfileByColleagueUuid(
            @PathVariable UUID colleagueUuid,
            @RequestParam(required = false) Set<UserIncludes> includes) {
        return RestResponse.success(profileService.findProfileByColleagueUuid(colleagueUuid, requireNonNullElse(includes, emptySet()))
                .orElseThrow(() -> notFound("colleagueUuid", colleagueUuid)));
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(USER_NOT_FOUND.getCode(), messages.getMessage(USER_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
