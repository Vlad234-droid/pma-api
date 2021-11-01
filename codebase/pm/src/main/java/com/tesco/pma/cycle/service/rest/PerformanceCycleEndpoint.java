package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PerformanceCycle;
import com.tesco.pma.cycle.service.PerformanceCycleService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping(path = "/performance-cycle")
public class PerformanceCycleEndpoint {

    private final PerformanceCycleService service;

    /**
     * POST call to create a Performance cycle.
     *
     * @param cycle a PerformanceCycle
     * @return a RestResponse parameterized with PerformanceCycle
     */
    @Operation(summary = "Create performance cycle", description = "Performance cycle created", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PerformanceCycle> createPerformanceCycle(@RequestBody PerformanceCycle cycle) {

        return success(service.create(cycle));
    }

    @Operation(summary = "Update performance cycle status", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Updated performance cycle status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycle not found", content = @Content)
    @PutMapping(value = "/{cycleUuid}/statuses/{status}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PerformanceCycle> updateCycleStatus(@PathVariable("cycleUuid") UUID cycleUuid,
                                                            @PathVariable("status") PMCycleStatus status) {

        return success(service.updateStatus(cycleUuid, status));
    }

    @Operation(summary = "Get all performance cycles for status", tags = {"performance-cycle"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found all performance cycles with status")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Performance cycles for the status not found",
            content = @Content)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<PerformanceCycle>> getAllPerformanceCyclesForStatus(@RequestParam("status") PMCycleStatus status) {
        return success(service.getAllPerformanceCyclesForStatus(status));
    }


}
