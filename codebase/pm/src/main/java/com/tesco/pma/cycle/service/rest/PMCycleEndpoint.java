package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.exception.InvalidPayloadException;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Map;
import java.util.Objects;
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
    private final AuditorAware<UUID> auditorAware;
    private final NamedMessageSourceAccessor messageSourceAccessor;


    /**
     * POST call to create a Performance Cycle .
     *
     * @param cycle a PMCycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Create performance cycle",
            description = "Performance cycle created",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Performance cycle created")
    @PostMapping(value = "/pm-cycles", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<PMCycle> create(@RequestBody PMCycle cycle) {
        return success(service.create(cycle, resolveUserUuid()));
    }

    /**
     * PUT call to create and run Performance Cycle.
     *
     * @param cycle a PMCycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Publish performance cycle",
            description = "Performance cycle published",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "SPerformance cycle published")
    @PutMapping(value = "/pm-cycles/publish", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<PMCycle> publish(@RequestBody PMCycle cycle) {

        return success(service.publish(cycle, resolveUserUuid()));
    }

    /**
     * {@code PUT  /pm-cycles/:uuid} : Updates an existing performance cycle.
     *
     * @param uuid  the uuid of the performance cycle to update.
     * @param cycle the performance cycle to update.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the updated performance cycle,
     * or with status {@code 400 (Bad Request)} if the performance cycle is not valid,
     * or with status {@code 500 (Internal Server Error)} if the performance cycle couldn't be updated.
     * @throws InvalidParameterException InvalidParameterException
     * @throws InvalidPayloadException   InvalidPayloadException
     */
    @Operation(summary = "Updates an existing performance cycle",
            description = "Performance cycle edited",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle updated")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid UUID")
    @PutMapping(value = "/pm-cycles/{uuid}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<PMCycle> update(@PathVariable(value = "uuid", required = false) final UUID uuid,
                                        @RequestBody PMCycle cycle) {
        if (cycle.getUuid() == null) {
            throw new InvalidPayloadException(HttpStatusCodes.BAD_REQUEST, "UUID must not be null", "pm-cycle.uuid");
        }
        if (!Objects.equals(uuid, cycle.getUuid())) {
            throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, "Path uuid does not match body uuid", "pm-cycle.uuid");
        }

        return success(service.update(cycle));
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
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
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
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isProcessManager() or isAdmin()")
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
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<PMCycle> get(@PathVariable("uuid") UUID uuid) {
        return success(service.get(uuid));
    }

    @Operation(summary = "Get full metadata for colleague",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle metadata")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @GetMapping(value = "/colleagues/{colleagueUuid}/metadata", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
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
    @PreAuthorize("isAdmin()")
    public RestResponse<?> updateJsonMetadata(@PathVariable("uuid") UUID uuid,
                                              @RequestBody String metadata) {
        service.updateJsonMetadata(uuid, metadata);
        return RestResponse.success();
    }

    @Operation(summary = "Get performance cycle metadata by file UUID",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle metadata by file UUID")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle metadata not found",
            content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Exception while parsing a form")
    @GetMapping(value = "/pm-cycles/files/{uuid}/metadata", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<PMCycleMetadata> getPmCycleMetadata(@PathVariable("uuid") UUID uuid) {
        return success(service.getFileMetadata(uuid));
    }

    /**
     * PUT call to deploy Performance Cycle.
     *
     * @param cycle a PMCycle
     * @return id of deployed process definition
     */
    @Operation(summary = "Deploy performance cycle",
            description = "Performance cycle deployed",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle deployed")
    @PutMapping(value = "/pm-cycles/{uuid}/deploy", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RestResponse<String> deploy(@PathVariable("uuid") UUID uuid,
                                       @RequestBody PMCycle cycle) {

        return success(service.deploy(cycle));
    }

    /**
     * PUT call to start Performance Cycle.
     *
     * @param cycleUUID a PMCycle uuid
     * @param processId process id
     * @return sucess
     */
    @Operation(summary = "Start performance cycle",
            description = "Performance cycle started",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle started")
    @PutMapping(value = "/pm-cycles/{uuid}/start", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RestResponse<?> start(@PathVariable("uuid") UUID cycleUUID,
                                 @RequestBody String processId) {

        service.start(cycleUUID, processId);
        return RestResponse.success();
    }

    private String jsonMetadataToRestResponse(String jsonMetadata) {
        return "{\"success\": true, \"data\": " + jsonMetadata + "}";
    }

    private UUID resolveUserUuid() {
        return auditorAware.getCurrentAuditor();
    }
}
