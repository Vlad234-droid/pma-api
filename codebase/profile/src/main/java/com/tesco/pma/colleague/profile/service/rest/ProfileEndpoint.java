package com.tesco.pma.colleague.profile.service.rest;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.domain.ImportRequest;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.exception.ImportException;
import com.tesco.pma.colleague.profile.service.ImportColleagueService;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.rest.HttpStatusCodes.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/colleagues", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class ProfileEndpoint {

    private final ProfileService profileService;
    private final ImportColleagueService importService;
    private final NamedMessageSourceAccessor messages;

    @Operation(summary = "Get profile by colleague uuid", description = "Get profile by colleague uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found")
    @GetMapping(path = "/{colleagueUuid}")
    public RestResponse<ColleagueProfile> getProfileByColleagueUuid(@PathVariable UUID colleagueUuid) {
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
    @PutMapping(path = "{colleagueUuid}/attributes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<TypedAttribute>> updateProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                      @RequestBody @Valid List<TypedAttribute> profileAttributes) {
        return RestResponse.success(profileService.updateProfileAttributes(colleagueUuid, profileAttributes));
    }

    /**
     * POST call to create profile attributes.
     *
     * @param colleagueUuid     an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Create new profile attributes", description = "Profile attributes created", tags = {"profile"})
    @ApiResponse(responseCode = CREATED, description = "Successful operation")
    @PostMapping(path = "{colleagueUuid}/attributes", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<TypedAttribute>> createProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                      @RequestBody @Valid List<TypedAttribute> profileAttributes) {
        return RestResponse.success(profileService.createProfileAttributes(colleagueUuid, profileAttributes));
    }


    /**
     * DELETE call to delete profile attributes.
     *
     * @param colleagueUuid     an identifier
     * @param profileAttributes profile attributes
     * @return a RestResponse parameterized with profile attributes
     */
    @Operation(summary = "Delete existing profile attributes", description = "Delete existing profile attributes", tags = {"profile"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Profile attributes deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Profile not found", content = @Content)
    @DeleteMapping(path = "{colleagueUuid}/attributes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<TypedAttribute>> deleteProfileAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                      @RequestBody @Valid List<TypedAttribute> profileAttributes) {
        return RestResponse.success(profileService.deleteProfileAttributes(colleagueUuid, profileAttributes));
    }

    private NotFoundException notFound(String paramName, Object paramValue) {
        return new NotFoundException(ErrorCodes.PROFILE_NOT_FOUND.getCode(), messages.getMessage(ErrorCodes.PROFILE_NOT_FOUND, Map.of(
                "param_name", paramName, "param_value", paramValue)));
    }

    @Operation(
            summary = "Start import colleagues process",
            tags = "profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding =
                            @Encoding(name = "file", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            ),
            responses = @ApiResponse(responseCode = CREATED, description = "Colleagues was imported")
    )
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<ImportReport> importColleagues(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new InvalidPayloadException("INVALID_PAYLOAD", "File cannot be empty", "content");
        }

        try (var inputStream = file.getInputStream()) {
            return RestResponse.success(importService.importColleagues(inputStream, file.getOriginalFilename()));
        } catch (IOException e) {
            throw new ImportException("Failed to import colleagues", e);
        }
    }

    @Operation(summary = "Get import request by uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Request found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Request not found")
    @GetMapping(path = "/requests/{requestUuid}")
    public RestResponse<ImportRequest> getImportRequest(@PathVariable UUID requestUuid) {
        return RestResponse.success(importService.getRequest(requestUuid));
    }

    @Operation(summary = "Get import request errors by request uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Request errors found")
    @GetMapping(path = "/requests/{requestUuid}/errors")
    public RestResponse<List<ImportError>> getImportRequestErrors(@PathVariable UUID requestUuid) {
        return RestResponse.success(importService.getRequestErrors(requestUuid));
    }

    @Operation(summary = "Autocomplete search among colleagues by full name", tags = {"colleagues"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Search among colleagues by full name")
    @GetMapping(value = "/suggestions", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, params = {"fullName"})
    public RestResponse<List<Colleague>> getSuggestionsFullName(@RequestParam String fullName) {
        return RestResponse.success(profileService.getSuggestions(fullName, null));
    }

    @Operation(summary = "Autocomplete search among colleagues by full name and manager ID", tags = {"colleagues"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Search among colleagues by full name and manager ID")
    @GetMapping(value = "/suggestions", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, params = {"fullName", "managerId"})
    public RestResponse<List<Colleague>> getSuggestionsAmongSubordinatesFullName(@RequestParam String fullName,
                                                                                 @RequestParam UUID managerId) {
        return RestResponse.success(profileService.getSuggestions(fullName, managerId));
    }

}
