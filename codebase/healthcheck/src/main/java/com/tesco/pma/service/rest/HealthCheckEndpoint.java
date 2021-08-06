package com.tesco.pma.service.rest;

import com.tesco.pma.service.LivenessHealthIndicator;
import com.tesco.pma.service.OverallHealthIndicator;
import com.tesco.pma.service.ReadinessHealthIndicator;
import com.tesco.pma.healthcheck.Health;
import com.tesco.pma.healthcheck.OverallHealth;

import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Health check indicators endpoint
 */
@RestController
@RequestMapping
@Validated
@RequiredArgsConstructor
public class HealthCheckEndpoint {

    private final OverallHealthIndicator overallHealthIndicator;

    private final ReadinessHealthIndicator readinessHealthIndicator;

    private final LivenessHealthIndicator livenessHealthIndicator;

    @Operation(summary = "Return a summary of the health check results for API Domain", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return a summary of the health check results")
    @GetMapping(path = "_status", produces = APPLICATION_JSON_VALUE)
    public Health getStatus() {
        return overallHealthIndicator.health();
    }

    @Operation(summary = "Return the status of API domain and all its components and dependencies", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return the status of API domain and all its components, dependencies")
    @GetMapping(path = "_healthcheck", produces = APPLICATION_JSON_VALUE)
    public OverallHealth getOverallHealthCheck() {
        return overallHealthIndicator.overallHealth();
    }

    @Operation(summary = "Return a successful response if this instance is able to receive traffic", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return a successful response if this node is able to receive traffic")
    @GetMapping(path = "_ready", produces = APPLICATION_JSON_VALUE)
    public Health getReady() {
        return readinessHealthIndicator.health();
    }

    @Operation(summary = "Return a successful response if this instance is able to receive traffic", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return a successful response if this node is able to receive traffic")
    @GetMapping(path = "hc", produces = APPLICATION_JSON_VALUE)
    public Health getHc() {
        return readinessHealthIndicator.health();
    }

    @Operation(summary = "Return a successful response in the case that the API Domain is in a healthy working state", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return a successful response "
            + "in the case that the API Domain is in a healthy working state")
    @GetMapping(path = "_working", produces = APPLICATION_JSON_VALUE)
    public Health getWorking() {
        return livenessHealthIndicator.health();
    }

    @Operation(summary = "Return a successful response in the case that the API Domain is in a healthy working state", tags = "diagnostic")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Return a successful response "
            + "in the case that the API Domain is in a healthy working state")
    @GetMapping(path = "live", produces = APPLICATION_JSON_VALUE)
    public Health getLive() {
        return livenessHealthIndicator.health();
    }
}