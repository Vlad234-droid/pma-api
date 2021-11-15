package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.exception.ErrorCodes.PM_CYCLE_METADATA_NOT_FOUND;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PMCycleEndpoint {

    private static final String CYCLE_UUID_PARAMETER_NAME = "cycleUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    public static final String INCLUDE_METADATA = "includeMetadata";

    private final PMCycleService service;
    private final NamedMessageSourceAccessor messageSourceAccessor;


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
     * PUT call to create and run Performance Cycle.
     *
     * @param config a PMCycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Publish performance cycle",
            description = "Performance cycle published",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.ACCEPTED, description = "Successful operation")
    @PutMapping(value = "/pm-cycles/publish", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RestResponse<PMCycle> publish(@RequestBody PMCycle config) {

        return success(service.publish(config));
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
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @PatchMapping(value = "/pm-cycles/{uuid}/statuses/{status}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> updateStatus(@PathVariable("uuid") UUID uuid,
                                              @PathVariable("status") PMCycleStatus status) {

        return success(service.updateStatus(uuid, status));
    }

    /**
     * Get call using a includeMetadata param and return a list of PMCycle as JSON.
     *
     * @param includeMetadata includeMetadata (true/false)
     * @return a RestResponse parameterized with list of PMCycle
     */
    @Operation(summary = "Get all performance cycles for status",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycles with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycles for the status not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles/", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycle>> getAll(@RequestParam(value = INCLUDE_METADATA, defaultValue = "false")
                                                          boolean includeMetadata) {
        return success(service.getAll(includeMetadata));
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
        return RestResponse.success(service.getCycleTimelineByColleague(colleagueUuid));
    }

    @Operation(summary = "Get full metadata for colleague",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle metadata")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @GetMapping(value = "/colleagues/{colleagueUuid}/metadata", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMetadataByColleague(@PathVariable UUID colleagueUuid) {
        var cycle = service.getCurrentByColleague(colleagueUuid);
        var metadata = cycle.getJsonMetadata();
        if (StringUtils.isBlank(metadata)) {
            throw new NotFoundException(PM_CYCLE_METADATA_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PM_CYCLE_METADATA_NOT_FOUND,
                            Map.of(CYCLE_UUID_PARAMETER_NAME, cycle.getUuid(),
                                    COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid)));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonMetadataToRestResponse(metadata));
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

    private String jsonMetadataToRestResponse(String jsonMetadata) {
        return "{\"success\": true, \"data\": " + jsonMetadata + "}";
    }
}
