package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PMCycleEndpoint {

    private final PMCycleService service;

    /**
     * POST call to create a Performance Cycle .
     *
     * @param config a PMCycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Create performance cycle",
            description = "Performance cycle created",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(value = "/pm-cycles", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PMCycle> create(@RequestBody PMCycle config) {

        return success(service.create(config));
    }

    /**
     * PATCH call to change PMCycle status
     *
     * @param uuid   an identifier of performance cycle
     * @param status new status for PMCycle
     * @return a RestResponse parameterized with updated PMCycle
     */
    @Operation(summary = "Update performance cycle status",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Updated performance cycle  status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle  not found",
            content = @Content)
    @PatchMapping(value = "/pm-cycles/{uuid}/statuses/{status}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> updateStatus(@PathVariable("uuid") UUID uuid,
                                              @PathVariable("status") PMCycleStatus status) {

        return success(service.updateStatus(uuid, status));
    }

    /**
     * Get call using a Status param and return a list of PMCycle as JSON.
     *
     * @param status PMCycle status
     * @return a RestResponse parameterized with list of PMCycle
     */
    @Operation(summary = "Get all performance cycles for status",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycles with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycles for the status not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycle>> getByStatus(
            @RequestParam("status") PMCycleStatus status) {
        return success(service.getByStatus(status));
    }

    /**
     * Get call using a UUID param and return a PMCycle as JSON.
     *
     * @param uuid an identifier of performance cycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Get performance cycle by UUID",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle by UUID")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> get(@PathVariable("uuid") UUID uuid) {
        return success(service.get(uuid));
    }

    @Operation(summary = "Get cycle timeline by the cycle identifier",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle timeline")
    @GetMapping(value = "/pm-cycles/{uuid}/timeline", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycleTimelinePoint>> getTimeline(@PathVariable UUID uuid) {
        return RestResponse.success(service.getCycleTimeline(uuid));
    }

    @Operation(summary = "Get cycle timeline for colleague",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle timeline")
    @GetMapping(value = "/colleagues/{colleagueUuid}/timeline", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycleTimelinePoint>> getTimelineByColleague(@PathVariable UUID colleagueUuid) {
        var uuid = service.getCurrentByColleague(colleagueUuid);
        return RestResponse.success(service.getCycleTimeline(uuid.getUuid()));
    }

    @Operation(summary = "Get full metadata for colleague",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle metadata")
    @GetMapping(value = "/colleagues/{colleagueUuid}/metadata", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public RestResponse<String> getMetadataByColleague(@PathVariable UUID colleagueUuid) {
        var cycle = service.getCurrentByColleague(colleagueUuid);
        return RestResponse.success(cycle.getJsonMetadata());
    }

    // todo remove after UAT metadata should be stored with cycle together
    @Operation(summary = "[UAT] Store cycle metadata",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Store cycle metadata")
    @PutMapping(path = "/pm-cycles/{uuid}/metadata", produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<?> updateJsonMetadata(@PathVariable("uuid") UUID uuid,
                                             @RequestBody String metadata) {
        service.updateJsonMetadata(uuid, metadata);
        return RestResponse.success();
    }
}
