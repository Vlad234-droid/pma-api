package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.api.CompositePMCycleMetadataResponse;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.request.PMCycleUpdateFormRequest;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.cycle.service.PMCycleMappingService;
import com.tesco.pma.exception.DeploymentException;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;

import static com.tesco.pma.flow.FlowParameters.COLLEAGUE_UUIDS;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PMCycleEndpoint {

    public static final String INCLUDE_METADATA = "includeMetadata";
    public static final String INCLUDE_FORMS = "includeForms";
    public static final String PM_CYCLE_ASSIGNMENT = "pm_cycle_assignment";

    private final PMCycleService service;
    private final ProcessManagerService processManagerService;
    private final PMColleagueCycleService pmColleagueCycleService;
    private final PMCycleMappingService pmCycleMappingService;
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
    @PostMapping(value = "/pm-cycles/publish", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
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
    @Operation(summary = "Get all performance cycles",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycles with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycles for the status not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles/", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<List<PMCycle>> getAll(@Parameter(example = "{\n"
            + "   \"uuid\":\"3fa85f64-5717-4562-b3fc-2c963f66af10\",\n"
            + "   \"entry-config-key_nin\":[\n"
            + "      \"group_a1\",\n"
            + "      \"group_c\"\n"
            + "   ],\n"
            + "   \"template-uuid_in\":[\n"
            + "      \"3fa85f64-5717-4562-b3fc-2c963f66af10\",\n"
            + "      \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n"
            + "   ],\n"
            + "   \"name_ne\":\"test1\",\n"
            + "   \"status_in\":[\n"
            + "      \"ACTIVE\",\n"
            + "      \"INACTIVE\"\n"
            + "   ],\n"
            + "   \"type_ne\":\"HIRING\",\n"
            + "   \"created-by\":\"3fa85f64-5717-4562-b3fc-2c963f66afa4\",\n"
            + "   \"start-time_lt\":\"2021-11-26T14:18:42.615Z\",\n"
            + "   \"start-time_lte\":\"2021-11-26T14:18:42.615Z\",\n"
            + "   \"end-time_gt\":\"2021-11-25T14:36:33.587Z\",\n"
            + "   \"end-time_gte\":\"2021-11-25T14:36:33.587Z\"\n"
            + "}") RequestQuery requestQuery,
                                              @RequestParam(value = INCLUDE_METADATA, defaultValue = "false")
                                                      boolean includeMetadata) {
        return success(service.findAll(requestQuery, includeMetadata));
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
    public RestResponse<CompositePMCycleResponse> get(@PathVariable("uuid") UUID uuid,
                                                      @RequestParam(value = INCLUDE_FORMS, defaultValue = "false")
                                                              boolean includeForms) {
        return success(service.get(uuid, includeForms));
    }

    @Operation(summary = "Get full metadata for colleague",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle metadata")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @GetMapping(value = "/colleagues/{colleagueUuid}/metadata", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @PreAuthorize("isCurrentUser(#colleagueUuid) or isManagerOf(#colleagueUuid,2) or isProcessManager() or isAdmin()")
    public RestResponse<CompositePMCycleMetadataResponse> getMetadataByColleague(@PathVariable UUID colleagueUuid,
                                                                                 @RequestParam(value = INCLUDE_FORMS,
                                                                                         defaultValue = "false")
                                                                                         boolean includeForms) {
        return success(service.getCurrentMetadataByColleague(colleagueUuid, includeForms));
    }

    @Operation(summary = "Get performance cycle metadata by file UUID",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle metadata by file UUID")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle metadata not found",
            content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Exception while parsing a form")
    @GetMapping(value = "/pm-cycles/files/{uuid}/metadata", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<CompositePMCycleMetadataResponse> getPmCycleMetadata(@PathVariable("uuid") UUID uuid,
                                                                             @RequestParam(value = INCLUDE_FORMS, defaultValue = "false")
                                                                                     boolean includeForms) {
        return success(service.getFileMetadata(uuid, includeForms));
    }

    /**
     * PUT call to deploy Performance Cycle.
     *
     * @param uuid cycle uuid
     * @return id of deployed runtime process
     */
    @Operation(summary = "Deploy performance cycle",
            description = "Performance cycle deployed",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle deployed")
    @PostMapping(value = "/pm-cycles/{uuid}/deploy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<UUID> deploy(@PathVariable final UUID uuid) {

        return success(service.deploy(uuid));
    }

    /**
     * PUT call to start scheduled performance cycle.
     *
     * @param uuid cycle uuid
     * @return sucess
     */
    @Operation(summary = "Start performance cycle",
            description = "Performance cycle started",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle started")
    @PutMapping(value = "/pm-cycles/{uuid}/start-scheduled")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<Void> startScheduled(@PathVariable final UUID uuid) {
        log.debug("REST request to start cycle : {}", uuid);
        service.start(uuid);
        return success();
    }

    /**
     * PUT call to start colleague cycle.
     *
     * @param cycleUuid     cycle uuid
     * @param colleagueUuid colleague uuid
     * @return sucess
     */
    @Operation(summary = "Start performance cycle for colleague",
            description = "Performance cycle started",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle started")
    @PutMapping(value = "/pm-cycles/{cycleUuid}/colleagues/{colleagueUuid}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<Void> startColleagueCycle(@PathVariable final UUID cycleUuid,
                                                  @PathVariable final UUID colleagueUuid) {
        log.debug("REST request to start cycle : {}, colleague: {}", cycleUuid, colleagueUuid);
        pmColleagueCycleService.start(cycleUuid, colleagueUuid);
        return success();
    }

    @Operation(summary = "Update form",
            description = "Update performance cycle form",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Performance cycle form updated")
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    @PutMapping(value = "/pm-cycles/{uuid}/forms", produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> updateForm(@PathVariable("uuid") final UUID cycleUuid,
                                            @RequestBody PMCycleUpdateFormRequest updateFormRequest) {

        return success(service.updateForm(cycleUuid, updateFormRequest));
    }

    @Operation(summary = "Update form to the latest version",
            description = "Update form to the latest version",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Form updated to the latest version")
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    @PutMapping(value = "/pm-cycles/{uuid}/forms/latest", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> updateFormLatestVersion(@PathVariable("uuid") final UUID cycleUuid,
                                                         @RequestParam(value = "form-key") String formKey) {

        return success(service.updateFormToLatestVersion(cycleUuid, formKey));
    }

    @Operation(summary = "Run cycle assignment process", tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Started cycle assignment process")
    @PostMapping(value = "/pm-cycles/assignment", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<String> runCycleAssignmentProcess(@RequestBody @NotEmpty List<@NotEmpty String> colleagues) {
        var parameters = Map.of(COLLEAGUE_UUIDS.name(), colleagues);

        try {
            return RestResponse.success(processManagerService.runProcess(PM_CYCLE_ASSIGNMENT, parameters));
        } catch (ProcessExecutionException e) {
            throw deploymentException(PM_CYCLE_ASSIGNMENT, parameters, e);
        }
    }

    @Operation(summary = "Get performance cycle mapping keys",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle mapping keys")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle mapping keys not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles/mappings/keys", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Set<String>> getPmCycleMappingKey() {
        return success(pmCycleMappingService.getPmCycleMappingKeys());
    }


    @Operation(summary = "Get performance cycle mapping keys per colleagues",
            tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle mapping keys per colleagues")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle mapping keys not found",
            content = @Content)
    @GetMapping(value = "/pm-cycles/mappings/keys/colleagues", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<Map<UUID, String>> getPmCycleMappingKeyByColleagues(@RequestParam List<UUID> uuids) {
        return success(pmCycleMappingService.getPmCycleMappingKeys(uuids));
    }

    private DeploymentException deploymentException(String processKey, Map<String, ?> parameters, Throwable cause) {
        return new DeploymentException(PMProcessErrorCodes.PROCESS_CANNOT_BE_STARTED.getCode(),
                messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_CANNOT_BE_STARTED,
                        Map.of("processKey", processKey, "parameters", parameters)), "processes", cause);
    }

    private UUID resolveUserUuid() {
        return auditorAware.getCurrentAuditor();
    }
}
