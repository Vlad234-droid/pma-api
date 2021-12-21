package com.tesco.pma.pdp.rest;

import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.pdp.domain.PDPGoal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/pdp")
@RequiredArgsConstructor
public class PDPEndpoint {

    /**
     * POST call to create a PDP.
     *
     * @param colleagueUuid an identifier of colleague
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Create a PDP", description = "PDP created", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/{colleagueUuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<PDPGoal>> createPDP(@PathVariable("colleagueUuid") UUID colleagueUuid, @RequestBody List<PDPGoal> goals) {
        return null;
    }

    /**
     * POST call to update a PDP.
     *
     * @param colleagueUuid an identifier of colleague
     * @param goals         a list of PDP goals
     * @return a RestResponse parameterized with Goals of PDP
     */
    @Operation(summary = "Update a PDP", description = "PDP updated", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP updated")
    @PutMapping(path = "/{colleagueUuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<PDPGoal>> updatePDP(@PathVariable("colleagueUuid") UUID colleagueUuid, @RequestBody List<PDPGoal> goals) {
        return null;
    }

    /**
     * POST call to create a PDP Goal.
     *
     * @param colleagueUuid an identifier of colleague
     * @param goal          a PDP goal
     * @return a RestResponse parameterized with Goal of PDP
     */
    @Operation(summary = "Create a PDP Goal", description = "PDP Goal created", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/{colleagueUuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PDPGoal> addPDPGoal(@PathVariable("colleagueUuid") UUID colleagueUuid, @RequestBody PDPGoal goal) {
        return null;
    }

    /**
     * DELETE call to delete a PDP Goal.
     *
     * @param colleagueUuid an identifier of colleague
     * @param number        a sequence number of PDP Goal
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing PDP", description = "Delete existing PDP", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PDP deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP not found", content = @Content)
    @DeleteMapping(path = "/colleagues/{colleagueUuid}/numbers/{number}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Void> deletePDP(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                           @PathVariable("number") Integer number) {
        return null;
    }

    /**
     * Get call using a Path params and return a PDP Goal as JSON.
     *
     * @param colleagueUuid an identifier of colleague
     * @param number        a sequence number of PDP Goal
     * @return a RestResponse parameterized with PDP Goal
     */
    @Operation(summary = "Get a PDP Goal by its colleagueUuid and number", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goal")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goal not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/numbers/{number}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PDPGoal> getPDPGoal(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                            @PathVariable("number") Integer number) {
        return null;
    }

    /**
     * Get call using a Path params and return a PDP Goal as JSON.
     *
     * @param colleagueUuid an identifier of colleague
     * @return a RestResponse parameterized with PDP Goal
     */
    @Operation(summary = "Get a list of PDP Goal by its colleagueUuid", tags = {"pdp"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PDP Goals")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PDP Goals not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<PDPGoal>> getPDPGoalsByColleague(@PathVariable("colleagueUuid") UUID colleagueUuid) {
        return null;
    }
}
