package com.tesco.pma.service.deployment.rest;

import java.io.FileNotFoundException;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.DeploymentException;
import com.tesco.pma.exception.InitializationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.RestResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.06.2021 Time: 18:20
 */
@Slf4j
@RestController
@RequestMapping("/processes")
public class DeploymentEndpoint {
    @Autowired
    ProcessManagerService processManagerService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<String>> processes() {
        return RestResponse.success(processManagerService.listProcesses());
    }

    @PostMapping(
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<String> deployProcess(@RequestBody @NotNull @NotBlank String diagramPath) {
        try {
            return RestResponse.success(processManagerService.deployProcess(diagramPath));
        } catch (FileNotFoundException e) {
            throw new NotFoundException(ErrorCodes.ERROR_FILE_NOT_FOUND.name(), "Diagram file was not found", diagramPath, e);
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), "Process initialization failed", diagramPath, e);
        }
    }

    @DeleteMapping(
            path = "/{processName}",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<String> undeployProcess(@PathVariable("processName") @NotNull @NotBlank String processName) {
        try {
            processManagerService.undeployProcess(processName);
            return RestResponse.success(processName);
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), "Process undeployment failed", processName, e);
        }
    }
}
