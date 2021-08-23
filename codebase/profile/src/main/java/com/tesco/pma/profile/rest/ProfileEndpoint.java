package com.tesco.pma.profile.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.Profile;
import com.tesco.pma.profile.service.ProfileService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.profile.exception.ErrorCodes.PROFILE_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/profiles", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class ProfileEndpoint {

    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messages;

    @Operation(summary = "Get profile by colleague uuid", description = "Get profile by colleague uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found")
    @GetMapping(path = "/{colleagueUuid}")
    public RestResponse<Profile> getProfileByColleagueUuid(@PathVariable UUID colleagueUuid) {
        return RestResponse.success(profileService.findProfileByColleagueUuid(colleagueUuid)
                .orElseThrow(() -> notFound("colleagueUuid", colleagueUuid)));
    }

    /**
     * PUT call to update profile attributes.
     *
     * @param colleagueUuid     an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Update existing Profile", description = "Update existing profile attributes", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile attributes updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found", content = @Content)
    @PutMapping(path = "/{colleagueUuid}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
//    @Validated({ValidationGroup.WithoutId.class, Default.class})
    public RestResponse<List<ProfileAttribute>> updateProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                        @RequestBody @Valid List<ProfileAttribute> profileAttributes) {
        return RestResponse.success(profileService.updateProfileAttributes(colleagueUuid, profileAttributes));
    }

    /**
     * POST call to create profile attributes.
     *
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Create new profile attributes", description = "Profile attributes created", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
//    @Validated({ValidationGroup.WithoutId.class, Default.class})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<ProfileAttribute>> createProfileAttributes(@RequestBody @Valid List<ProfileAttribute> profileAttributes) {
        return RestResponse.success(profileService.createProfileAttributes(profileAttributes));
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(PROFILE_NOT_FOUND.getCode(), messages.getMessage(PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
