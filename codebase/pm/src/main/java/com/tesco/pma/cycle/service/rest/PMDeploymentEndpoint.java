package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.cycle.service.DeploymentService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/deploy")
@RestController
public class PMDeploymentEndpoint {

    private final DeploymentService deploymentService;

    /**
     * PUT call to deploy file resource by uuid.
     *
     * @param fileUuid file uuid
     * @return id of deployed runtime process
     */
    @Operation(summary = "Deploy file resource by uuid",
            description = "File deployed",
            tags = {"deployment"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File deployed")
    @PostMapping(value = "{fileUuid}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RestResponse<String> deployByUuid(@PathVariable final UUID fileUuid) {

        return success(deploymentService.deploy(fileUuid));
    }

    /**
     * PUT call to deploy the last version of the file resource by path and filename.
     *
     * @param path     file path
     * @param fileName file name
     * @return id of deployed runtime process
     */
    @Operation(summary = "Deploy the last version of the file resource by path and filename",
            description = "File deployed",
            tags = {"deployment"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File deployed")
    @PostMapping(value = "/last")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RestResponse<String> deployByPathAndFilename(@RequestParam("path") String path,
                                                        @RequestParam("fileName") String fileName) {

        return success(deploymentService.deploy(path, fileName));
    }

}
