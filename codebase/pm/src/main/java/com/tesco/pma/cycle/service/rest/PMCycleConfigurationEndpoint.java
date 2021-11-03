package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.cycle.api.PMCycleConfigurationStatus;
import com.tesco.pma.cycle.api.PMCycleConfiguration;
import com.tesco.pma.cycle.service.PMCycleConfigurationService;
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
@RequestMapping(path = "/pm-cycle-config")
public class PMCycleConfigurationEndpoint {

    private final PMCycleConfigurationService service;

    /**
     * POST call to create a Performance Cycle Configuration.
     *
     * @param cycle a PMCycleConfiguration
     * @return a RestResponse parameterized with PMCycleConfiguration
     */
    @Operation(summary = "Create performance cycle configuration",
            description = "Performance cycle configuration created",
            tags = {"performance-cycle-configuration"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PMCycleConfiguration> createPerformanceCycleConfiguration(@RequestBody PMCycleConfiguration config) {

        return success(service.create(config));
    }

    /**
     * PATCH call to change PMCycleConfiguration status
     *
     * @param uuid   an identifier of performance cycle configuration
     * @param status new status for PMCycleConfiguration
     * @return a RestResponse parameterized with updated PMCycleConfiguration
     */
    @Operation(summary = "Update performance cycle configuration status",
            tags = {"performance-cycle-configuration"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Updated performance cycle configuration status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle configuration not found", content = @Content)
    @PatchMapping(value = "/{uuid}/statuses/{status}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycleConfiguration> updateCycleStatus(@PathVariable("uuid") UUID uuid,
                                                                @PathVariable("status") PMCycleConfigurationStatus status) {

        return success(service.updateStatus(uuid, status));
    }

    /**
     * Get call using a Status param and return a list of PMCycleConfiguration as JSON.
     *
     * @param status PMCycleConfiguration status
     * @return a RestResponse parameterized with list of PMCycleConfiguration
     */
    @Operation(summary = "Get all performance cycle configurations for status",
            tags = {"performance-cycle-configuration"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycle configurations with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle configurations for the status not found",
            content = @Content)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<PMCycleConfiguration>> getAllPMCycleConfigForStatus(@RequestParam("status") PMCycleConfigurationStatus status) {
        return success(service.getAllPMCycleConfigForStatus(status));
    }

    /**
     * Get call using a UUID param and return a PMCycleConfiguration as JSON.
     *
     * @param uuid an identifier of performance cycle configuration
     * @return a RestResponse parameterized with PMCycleConfiguration
     */
    @Operation(summary = "Get performance cycle configuration by UUID",
            tags = {"performance-cycle-configuration"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found performance cycle configuration by UUID")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle configuration not found",
            content = @Content)
    @GetMapping(value = "/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycleConfiguration> getPMCycleConfigByUUID(@PathVariable("uuid") UUID uuid) {
        return success(service.getPMCycleConfigByUUID(uuid));
    }

    /**
     * DELETE call to soft delete a PMCycleConfiguration.
     *
     * @param uuid an identifier of performance cycle configuration
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Soft delete performance cycle configuration by UUID",
            tags = {"performance-cycle-configuration"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Removed performance cycle configuration")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle configuration not found")
    @DeleteMapping(value = "/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> removePMCycleConfig(@PathVariable("uuid") UUID uuid) {
        service.updateStatus(uuid, PMCycleConfigurationStatus.REMOVED);
        return success();
    }


}
