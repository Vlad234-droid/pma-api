package com.tesco.pma.pdp.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMForm;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pdp.api.PDPGoal;
import com.tesco.pma.pdp.api.PDPResponse;
import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.util.ResourceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
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
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.rest.RestResponse.success;
import static com.tesco.pma.util.FileUtils.getFormName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping(path = "/pdp")
@Validated
@RequiredArgsConstructor
public class PDPEndpoint {

    private final PDPService pdpService;
    private final ResourceProvider resourceProvider;
    private final NamedMessageSourceAccessor messages;
    @Value("${tesco.application.pdp.template.key}")
    private String templateKey;
    @Value("${tesco.application.pdp.form.key}")
    private String formKey;

    /**
     * POST call to create a PDP with its Goals.
     *
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Create a PDP", description = "PDP created", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/goals", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<PDPGoal>> create(@RequestBody List<@Valid PDPGoal> goals,
                                              @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var colleagueUuid = getColleagueUuid(authentication);
        goals.forEach(goal -> goal.setColleagueUuid(colleagueUuid));
        return success(pdpService.createGoals(colleagueUuid, goals));
    }

    /**
     * POST call to update a PDP with its Goals.
     *
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Update a PDP", description = "PDP updated", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP updated")
    @PutMapping(path = "/goals", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<PDPGoal>> update(@RequestBody List<@Valid PDPGoal> goals,
                                              @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var colleagueUuid = getColleagueUuid(authentication);
        goals.forEach(goal -> goal.setColleagueUuid(colleagueUuid));
        return success(pdpService.updateGoals(colleagueUuid, goals));
    }

    /**
     * DELETE call to delete PDP Goal by its uuid.
     *
     * @param goalUuid an identifier of goal
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing PDP Goal from a Plan by its uuid", description = "Delete existing PDP Goal", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP Goal deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @DeleteMapping(path = "/goals/{goalUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Void> deleteGoal(@PathVariable("goalUuid") UUID goalUuid,
                                         @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        pdpService.deleteGoal(getColleagueUuid(authentication), goalUuid);
        return RestResponse.success();
    }

    /**
     * Get call using a Path params and return a PDP Goal by its colleagueUuid and number as JSON.
     *
     * @param number        a sequence number of PDP Goal
     * @return a RestResponse parameterized with PDP Goal
     */
    @Operation(summary = "Get a PDP Goal by its colleague and number", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goal")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @GetMapping(path = "/goals/numbers/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PDPResponse> getGoal(@PathVariable("number") Integer number,
                                         @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var goal = pdpService.getGoal(getColleagueUuid(authentication), number);
        return success(new PDPResponse(Arrays.asList(goal), getPMForm()));
    }

    /**
     * Get call using a Path params and return a PDP Goal by its uuid as JSON.
     *
     * @param goalUuid an identifier of goal
     * @return a RestResponse parameterized with PDP Goal
     */
    @Operation(summary = "Get a PDP Goal by its uuid", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goal")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @GetMapping(path = "/goals/{goalUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PDPResponse> getGoal(@PathVariable("goalUuid") UUID goalUuid,
                                         @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var goal = pdpService.getGoal(getColleagueUuid(authentication), goalUuid);
        return success(new PDPResponse(Arrays.asList(goal), getPMForm()));
    }

    /**
     * Get call using a Path params and return a list of PDP Goals by its colleague as JSON.
     *
     * @return a RestResponse parameterized with list of PDP Goals
     */
    @Operation(summary = "Get a list of PDP Goals by its colleague", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goals")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goals not found", content = @Content)
    @GetMapping(path = "/goals", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PDPResponse> getGoals(@CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var goals = pdpService.getGoals(getColleagueUuid(authentication));
        return success(new PDPResponse(goals, getPMForm()));
    }

    /**
     * Returns PDP template's file
     *
     * @return response with downloaded file
     * @throws NotFoundException if the template is not found
     */
    @Operation(summary = "Download PDP template file",
            description = "Download PDP template file",
            tags = "pdp")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File downloaded")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid id supplied", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File not found",
            content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = HttpStatusCodes.UNAUTHORIZED, description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.FORBIDDEN, description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Internal Server Error", content = @Content)
    @GetMapping("/template")
    @PreAuthorize("isColleague()")
    public ResponseEntity<Resource> downloadTemplate() {
        try {
            var path = FilenameUtils.getFullPathNoEndSeparator(templateKey);
            var fileName = FilenameUtils.getName(templateKey);
            var file = resourceProvider.readFile(path, fileName);
            var content = file.getFileContent();
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(content.length)
                    .body(new ByteArrayResource(content));
        } catch (IOException e) {
            throw new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "PDP template was not found", templateKey, e);
        }
    }

    /**
     * Get call that returns early achievement date of Goals as JSON.
     *
     * @return a RestResponse parameterized with the earliest achievement date of Goals
     */
    @Operation(summary = "Get the earliest achievement date of PDP Goals by its colleague", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found achievement date of Goal")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @GetMapping(path = "/goals/early-achievement-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<LocalDate> getEarlyAchievementDate(@CurrentSecurityContext(expression = "authentication")
                                                                       Authentication authentication) {
        return success(pdpService.getEarlyAchievementDate(getColleagueUuid(authentication)));
    }

    private UUID getColleagueUuid(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private PMForm getPMForm() {
        try {
            var formName = FilenameUtils.getName(getFormName(formKey));
            var formPath = FilenameUtils.getFullPathNoEndSeparator(formKey);
            var formFile = resourceProvider.readFile(formPath, formName);
            return new PMForm(formFile.getUuid().toString(), formKey, formKey, new String(formFile.getFileContent(), UTF_8));
        } catch (IOException e) {
            throw new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "Form was not found", formKey, e);
        }
    }
}