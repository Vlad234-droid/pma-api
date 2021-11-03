package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping(path = "/pm-cycles")
public class PMCycleEndpoint {

    private final PMCycleService service;

    /**
     * POST call to create a Performance Cycle.
     *
     * @param cycle a PMCycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Create performance cycle", description = "Performance cycle created", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PMCycle> createPerformanceCycle(@RequestBody PMCycle cycle) {

        return success(service.create(cycle));
    }

    /**
     * PATCH call to change PMCycle status
     *
     * @param uuid   an identifier of performance cycle
     * @param status new status for PMCycle
     * @return a RestResponse parameterized with updated PMCycle
     */
    @Operation(summary = "Update performance cycle status", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Updated performance cycle status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found", content = @Content)
    @PatchMapping(value = "/{uuid}/statuses/{status}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> updateCycleStatus(@PathVariable("uuid") UUID uuid,
                                                   @PathVariable("status") PMCycleStatus status) {

        return success(service.updateStatus(uuid, status));
    }

    /**
     * Get call using a Status param and return a list of PMCycle as JSON.
     *
     * @param status PMCycle status
     * @return a RestResponse parameterized with list of PMCycle
     */
    @Operation(summary = "Get all performance cycles for status", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycles with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycles for the status not found",
            content = @Content)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycle>> getAllPerformanceCyclesForStatus(@RequestParam("status") PMCycleStatus status) {
        return success(service.getAllPerformanceCyclesForStatus(status));
    }

    /**
     * Get call using a UUID param and return a PMCycle as JSON.
     *
     * @param uuid an identifier of performance cycle
     * @return a RestResponse parameterized with PMCycle
     */
    @Operation(summary = "Get performance cycle by UUID", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle by UUID")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found",
            content = @Content)
    @GetMapping(value = "/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycle> getPerformanceCycleByUUID(@PathVariable("uuid") UUID uuid) {
        return success(service.getPerformanceCycle(uuid));
    }

    /**
     * DELETE call to soft delete a PMCycle.
     *
     * @param uuid an identifier of performance cycle
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Soft delete performance cycle by UUID", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Removed performance cycle")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found")
    @DeleteMapping(value = "/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> removePerformanceCycle(@PathVariable("uuid") UUID uuid) {
        service.updateStatus(uuid, PMCycleStatus.REMOVED);
        return success();
    }


}
