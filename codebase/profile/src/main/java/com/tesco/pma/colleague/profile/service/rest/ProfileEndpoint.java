package com.tesco.pma.colleague.profile.service.rest;

import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.rest.model.AggregatedColleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/colleagues", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class ProfileEndpoint {

    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messages;

    @Operation(summary = "Get profile by colleague uuid", description = "Get profile by colleague uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found")
    @GetMapping(path = "/{colleagueUuid}")
    public RestResponse<AggregatedColleague> getProfileByColleagueUuid(@PathVariable UUID colleagueUuid) {
        return RestResponse.success(profileService.findProfileByColleagueUuid(colleagueUuid)
                .orElseThrow(() -> notFound("colleagueUuid", colleagueUuid)));
    }

    /**
     * PUT call to update profile attributes.
     *
     * @param colleagueUuid an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Update existing Profile", description = "Update existing profile attributes", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile attributes updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found", content = @Content)
    @PutMapping(path = "{colleagueUuid}/attributes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<ProfileAttribute>> updateProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                        @RequestBody @Valid List<ProfileAttribute> profileAttributes) {
        return RestResponse.success(profileService.updateProfileAttributes(colleagueUuid, profileAttributes));
    }

    /**
     * POST call to create profile attributes.
     *
     * @param colleagueUuid an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Create new profile attributes", description = "Profile attributes created", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "{colleagueUuid}/attributes", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<ProfileAttribute>> createProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                        @RequestBody @Valid List<ProfileAttribute> profileAttributes) {
        return RestResponse.success(profileService.createProfileAttributes(colleagueUuid, profileAttributes));
    }


    /**
     * DELETE call to delete profile attributes.
     *
     * @param colleagueUuid an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Delete existing profile attributes", description = "Delete existing profile attributes", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile attributes deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found", content = @Content)
    @DeleteMapping(path = "{colleagueUuid}/attributes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<ProfileAttribute>> deleteProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                        @RequestBody @Valid List<ProfileAttribute> profileAttributes) {
        return RestResponse.success(profileService.deleteProfileAttributes(colleagueUuid, profileAttributes));
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(), messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

}
