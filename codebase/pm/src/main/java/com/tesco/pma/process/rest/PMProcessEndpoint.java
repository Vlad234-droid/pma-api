package com.tesco.pma.process.rest;

import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/processes")
public class PMProcessEndpoint {

    private final PMProcessService processService;

    @Operation(summary = "Get process metadata by process key, e.g., GROUPS_HO_S_WL1",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process metadata")
    @GetMapping(value = "metadata", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycleMetadata> getMetadata(@RequestParam(name = "process-key") String processKey) {
        return RestResponse.success(processService.getProcessMetadataByKey(processKey));
    }
}