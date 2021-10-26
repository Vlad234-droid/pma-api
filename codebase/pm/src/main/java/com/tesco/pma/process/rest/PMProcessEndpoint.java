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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/processes")
public class PMProcessEndpoint {

    private final PMProcessService processService;

    @Operation(summary = "Get process timeline by process identifier",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process timeline")
    @GetMapping(value = "{processUuid}/timeline", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<TimelineResponse>> getTimeline(@PathVariable UUID processUuid) {
        return RestResponse.success(processService.getProcessMetadata(processUuid));
    }

    @Operation(summary = "Store process metadata",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Store process metadata")
    @PostMapping(path = "/{processUuid}/metadata", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<?> storeProcessMetadata(@PathVariable("processUuid") UUID processUuid,
                                                @RequestBody String metadata) {
        processService.saveProcessMetadata(processUuid, metadata);
        return RestResponse.success();
    }

    @Operation(summary = "Get process metadata by process key, e.g., GROUPS_HO_S_WL1",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process metadata")
    @GetMapping(value = "metadata", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMProcessMetadata> getMetadata(@RequestParam(name = "process-key") String processKey) {
        return RestResponse.success(processService.getProcessMetadataByKey(processKey));
    }

    @Operation(summary = "Get full metadata from db by process identifier",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process metadata")
    @GetMapping(value = "{processUuid}/metadata", produces = APPLICATION_JSON_VALUE)
    public String getFullMetadata(@PathVariable UUID processUuid) {
        return processService.getFullMetadata(processUuid);
    }


}