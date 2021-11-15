package com.tesco.pma.process.rest;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.exception.DeploymentException;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/processes")
public class PMProcessEndpoint {

    private final PMProcessService processService;
    private final ProcessManagerService processManagerService;

    @Operation(summary = "Get process metadata by process key, e.g., TYPE_1, TYPE_2, TYPE_4",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the process metadata")
    @GetMapping(value = "metadata", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PMCycleMetadata> getMetadata(@RequestParam(name = "process-key") String processKey) {
        return RestResponse.success(processService.getProcessMetadataByKey(processKey));
    }

    @Operation(summary = "Run process metadata by process key with parameters",
            tags = {"processes"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Started process identifier")
    @PostMapping(value = "/keys/{process-key}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<String> runProcessByKey(@PathVariable("process-key") String processKey,
                                                @RequestParam
                                                @Parameter(in = ParameterIn.QUERY, name = "params",
                                                        style = ParameterStyle.FORM, schema = @Schema(type = "object"),
                                                        explode = Explode.TRUE,
                                                        example = "{\"p1\":\"v1\",\"p2\":\"v2\"") String paramsObj,
                                                HttpServletRequest request) {

        Map<String, String> parameters = request.getParameterMap() == null ? null
                : request.getParameterMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
        try {
            return RestResponse.success(processManagerService.runProcess(processKey, parameters));
        } catch (ProcessExecutionException e) {
            //todo
            throw new DeploymentException("PROCESS_CANNOT_BE_STARTED", "Process cannot be started by key "
                    + processKey + " with parameters " + parameters, "processes", e);
        }
    }
}