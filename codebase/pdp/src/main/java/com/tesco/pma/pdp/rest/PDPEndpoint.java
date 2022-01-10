package com.tesco.pma.pdp.rest;

import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.pdp.domain.PDPGoal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/pdp")
@Validated
@RequiredArgsConstructor
public class PDPEndpoint {

    private final PDPService pdpService;

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
    public RestResponse<PDPGoal> getGoal(@PathVariable("number") Integer number,
                                         @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return success(pdpService.getGoal(getColleagueUuid(authentication), number));
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
    public RestResponse<PDPGoal> getGoal(@PathVariable("goalUuid") UUID goalUuid,
                                         @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return success(pdpService.getGoal(getColleagueUuid(authentication), goalUuid));
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
    public RestResponse<List<PDPGoal>> getGoals(@CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return success(pdpService.getGoals(getColleagueUuid(authentication)));
    }

    private UUID getColleagueUuid(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}