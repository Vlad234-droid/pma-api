package com.tesco.pma.pdp.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMForm;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.util.ResourceProvider;
import com.tesco.pma.pdp.domain.PDPResponse;
import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.pdp.domain.PDPGoal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.Authentication;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.rest.RestResponse.success;
import static com.tesco.pma.util.FileUtils.getFormName;
import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping(path = "/pdp")
@Validated
@RequiredArgsConstructor
public class PDPEndpoint {

    private final PDPService pdpService;
    private final ResourceProvider resourceProvider;
    @Value("${tesco.application.pdp.form.key}")
    private String formKey;
    private final NamedMessageSourceAccessor messages;

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

    private UUID getColleagueUuid(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private PMForm getPMForm() {
        File formFile;

        var formName = FilenameUtils.getName(getFormName(formKey));
        var formPath = FilenameUtils.getFullPathNoEndSeparator(formKey);
        try {
            formFile = resourceProvider.readFile(formPath, formName);
        } catch (IOException e) {
            throw new NotFoundException(ERROR_FILE_NOT_FOUND.name(), "Form was not found", formName, e);
        }

        return new PMForm(formFile.getUuid().toString(), formKey, formKey, new String(formFile.getFileContent(), UTF_8));
    }
}