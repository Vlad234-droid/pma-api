package com.tesco.pma.process.rest;


import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.TimelineResponse;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/process-metadata")
public class TimelineEndpoint {

    private final PMProcessService processService;

    @Operation(summary = "Get process metadata by process identifier",
            tags = {"process-metadata"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process metadata")
    @GetMapping(value = "{processUuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<TimelineResponse>> getProcessMetadata(@PathVariable UUID processUuid) {
        return RestResponse.success(processService.getProcessMetadata(processUuid));
    }

    @Operation(summary = "Create process metadata",
            tags = {"process-metadata"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Created process metadata")
    @PostMapping(path = "/{processUuid}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<?> createProcessMetadata(@PathVariable("processUuid") UUID processUuid,
                                                 @RequestBody PMProcessMetadata metadata) {
        processService.saveProcessMetadata(processUuid, metadata);
        return RestResponse.success();
    }

}