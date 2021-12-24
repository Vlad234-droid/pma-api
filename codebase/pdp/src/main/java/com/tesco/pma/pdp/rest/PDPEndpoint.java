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
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;

@RestController
@RequestMapping(path = "/pdp")
@Validated
@RequiredArgsConstructor
public class PDPEndpoint {

    private final PDPService pdpService;

    /**
     * POST call to create a PDP with its Goals.
     *
     * @param colleagueUuid an identifier of colleague
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Create a PDP", description = "PDP created", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/colleagues/{colleagueUuid}/goals", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isCurrentUser(#colleagueUuid)")
    public RestResponse<List<PDPGoal>> create(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                              @RequestBody List<@Valid PDPGoal> goals) {
        return success(pdpService.create(colleagueUuid, goals));
    }

    /**
     * POST call to update a PDP with its Goals.
     *
     * @param colleagueUuid an identifier of colleague
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Update a PDP", description = "PDP updated", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP updated")
    @PutMapping(path = "/colleagues/{colleagueUuid}/goals", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isCurrentUser(#colleagueUuid)")
    public RestResponse<List<PDPGoal>> update(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                              @RequestBody List<@Valid PDPGoal> goals) {
        return success(pdpService.update(colleagueUuid, goals));
    }

    /**
     * DELETE call to delete PDP Goals by its colleague and numbers.
     *
     * @param colleagueUuid an identifier of colleague
     * @param numbers       a sequence number of PDP Goal
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing PDP Goals from a Plan by its colleague and numbers",
            description = "Delete existing PDP Goals", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP Goals deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goals not found", content = @Content)
    @DeleteMapping(path = "/colleagues/{colleagueUuid}/goals/numbers/{numbers}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isCurrentUser(#colleagueUuid)")
    public RestResponse<Void> deleteGoals(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                          @PathVariable("numbers") List<Integer> numbers) {
        pdpService.deleteGoals(numbers, colleagueUuid);
        return RestResponse.success();
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
    @DeleteMapping(path = "/colleagues/goals/{goalUuids}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Void> deleteGoals(@PathVariable("goalUuids") List<UUID> goalUuids, Authentication authentication) {
        pdpService.deleteGoals(UUID.fromString(authentication.getName()), goalUuids);
        return RestResponse.success();
    }

    /**
     * Get call using a Path params and return a PDP Goal by its colleagueUuid and number as JSON.
     *
     * @param colleagueUuid an identifier of colleague
     * @param number        a sequence number of PDP Goal
     * @return a RestResponse parameterized with PDP Goal
     */
    @Operation(summary = "Get a PDP Goal by its colleague and number", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goal")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/goals/numbers/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isCurrentUser(#colleagueUuid)")
    public RestResponse<PDPGoal> getGoal(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                         @PathVariable("number") Integer number) {
        return success(pdpService.getGoal(colleagueUuid, number));
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
    @GetMapping(path = "/colleagues/goals/{goalUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PDPGoal> getGoal(@PathVariable("goalUuid") UUID goalUuid, Authentication authentication) {
        return success(pdpService.getGoal(UUID.fromString(authentication.getName()), goalUuid));
    }

    /**
     * Get call using a Path params and return a list of PDP Goals by its colleague as JSON.
     *
     * @param colleagueUuid an identifier of colleague
     * @return a RestResponse parameterized with list of PDP Goals
     */
    @Operation(summary = "Get a list of PDP Goals by its colleague", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goals")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goals not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/goals", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isCurrentUser(#colleagueUuid)")
    public RestResponse<List<PDPGoal>> getGoals(@PathVariable("colleagueUuid") UUID colleagueUuid) {
        return success(pdpService.getGoals(colleagueUuid));
    }
}