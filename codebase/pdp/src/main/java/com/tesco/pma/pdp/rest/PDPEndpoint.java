package com.tesco.pma.pdp.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.model.PMForm;
import com.tesco.pma.cycle.exception.ParseException;
import com.tesco.pma.cycle.model.ResourceProvider;
import com.tesco.pma.error.ErrorCodeAware;
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
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.model.PMFormElement.KEY;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_PARSE_NOT_FOUND;
import static com.tesco.pma.cycle.model.PMProcessModelParser.getFormName;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/pdp")
@Validated
@RequiredArgsConstructor
public class PDPEndpoint {

    private final PDPService pdpService;
    private final ResourceProvider resourceProvider;
    @Value("${tesco.application.pdp.form.key}")
    private String pdpFormKey;
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
     * DELETE call to delete PDP Goals by its uuids.
     *
     * @param goalUuids an identifier of goals
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing PDP Goals from a Plan by its uuids", description = "Delete existing PDP Goal", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP Goals deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goals not found", content = @Content)
    @PostMapping(path = "/goals/delete", produces = MediaType.APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<Void> deleteGoals(@RequestBody List<UUID> goalUuids,
                                          @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        pdpService.deleteGoals(getColleagueUuid(authentication), goalUuids);
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
        String formJson;
        var formName = getFormName(pdpFormKey);
        try {
            formJson = resourceProvider.resourceToString(pdpFormKey, formName);
        } catch (IOException e) {
            throw parseException(PM_PARSE_NOT_FOUND, Map.of("key", KEY, "value", pdpFormKey), KEY, e);
        }


        var uuid = resourceProvider.readFileUuid(pdpFormKey, formName);
        return new PMForm(uuid.toString(), formName, formName, formJson);
    }

    private ParseException parseException(ErrorCodeAware errorCode, Map<String, ?> params, String field, Throwable cause) {
        return new ParseException(errorCode.getCode(), messages.getMessage(errorCode.getCode(), params), field, cause);
    }
}