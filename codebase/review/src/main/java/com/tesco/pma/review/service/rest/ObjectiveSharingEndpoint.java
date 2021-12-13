package com.tesco.pma.review.service.rest;

import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.service.ObjectiveSharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ObjectiveSharingEndpoint {

    private final ObjectiveSharingService objectiveSharingService;
    private final PMCycleService pmCycleService;

    private static final String CURRENT_PARAMETER_NAME = "CURRENT";

    @Operation(summary = "Share colleague objectives", tags = {"objective-sharing"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Objectives sharing have been enabled")
    @PostMapping(value = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/objective/sharing",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> shareObjectives(@PathVariable("cycleUuid") String pmCycle,
                                           @PathVariable("colleagueUuid") UUID colleagueUuid) {
        objectiveSharingService.shareObjectives(colleagueUuid, getPMCycleUuid(colleagueUuid, pmCycle));
        return RestResponse.success();
    }

    @Operation(summary = "Stop sharing colleague objectives", tags = {"objective-sharing"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Objectives sharing have been disabled")
    @DeleteMapping(value = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/objective/sharing",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> stopSharingObjectives(@PathVariable("cycleUuid") String pmCycle,
                                                 @PathVariable("colleagueUuid") UUID colleagueUuid) {
        objectiveSharingService.stopSharingObjectives(colleagueUuid, getPMCycleUuid(colleagueUuid, pmCycle));
        return RestResponse.success();
    }

    @Operation(summary = "Check if colleague objectives is shared", tags = {"objective-sharing"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Info about sharing objectives")
    @GetMapping(value = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/objective/sharing",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<Boolean> isColleagueShareObjectives(@PathVariable("cycleUuid") String pmCycle,
                                                            @PathVariable("colleagueUuid") UUID colleagueUuid) {
        return RestResponse.success(objectiveSharingService.isColleagueShareObjectives(
                colleagueUuid,
                getPMCycleUuid(colleagueUuid, pmCycle)));
    }

    @Operation(summary = "Get all shared objectives by their manager", tags = {"objective-sharing"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Shared objectives")
    @GetMapping(value = "/colleagues/{colleagueUuid}/review-types/objective/sharing", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Review>> getSharedObjectivesForColleague(@PathVariable("colleagueUuid") UUID colleagueUuid) {
        return RestResponse.success(objectiveSharingService.getSharedObjectivesForColleague(colleagueUuid));
    }

    private UUID getPMCycleUuid(UUID colleagueUuid, String cycleUuid) {
        if (cycleUuid.equalsIgnoreCase(CURRENT_PARAMETER_NAME)) {
            return pmCycleService.getCurrentByColleague(colleagueUuid).getUuid();
        } else {
            try {
                return UUID.fromString(cycleUuid);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, e.getMessage(), "cycleUuid"); // NOPMD
            }
        }
    }
}
