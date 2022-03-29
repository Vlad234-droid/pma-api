package com.tesco.pma.service.deployment.rest;

import com.tesco.pma.bpm.api.DeploymentInfo;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.cycle.service.DeploymentService;
import com.tesco.pma.exception.DeploymentException;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.InitializationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.rest.RestResponse.success;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.06.2021 Time: 18:20
 */
@Slf4j
@RestController
public class DeploymentEndpoint {

    private static final String PROCESS_UNDEPLOYMENT_FAILED = "Process undeployment failed";

    @Autowired
    ProcessManagerService processManagerService;

    @Autowired
    DeploymentService deploymentService;

    @Operation(summary = "Get list of deployed processes",
            tags = {"deployment"})
    @GetMapping(path = "/processes", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<List<String>> processes() {
        return RestResponse.success(processManagerService.listProcesses());
    }

    @Operation(summary = "Deploy process archive",
            tags = {"deployment"})
    @PostMapping(path = "/processes/archive",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<DeploymentInfo> deployProcess(@RequestBody @NotNull @NotBlank String diagramPath) {
        try {
            return RestResponse.success(processManagerService.deployProcessArchive(diagramPath));
        } catch (FileNotFoundException e) {
            throw new NotFoundException(ErrorCodes.ERROR_FILE_NOT_FOUND.name(), "Diagram file was not found", diagramPath, e);
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), "Process initialization failed", diagramPath, e);
        }
    }

    @Operation(summary = "Undeploy all version of process by process key(name)",
            tags = {"deployment"})
    @DeleteMapping(
            path = "/processes/{processName}",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<String> undeployProcess(@PathVariable("processName") @NotNull @NotBlank String processName) {
        try {
            processManagerService.undeployProcess(processName);
            return RestResponse.success(processName);
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), PROCESS_UNDEPLOYMENT_FAILED, processName, e);
        }
    }

    @Operation(summary = "Get list of deployments (identifier/name)",
            tags = {"deployment"})
    @GetMapping(path = "/deployments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<List<DeploymentInfo>> deployments() {
        return RestResponse.success(processManagerService.listDeployments());
    }

    @Operation(summary = "Deploy multipart files",
            tags = {"deployment"})
    @PostMapping(path = "/deployments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<DeploymentInfo> deploy(@RequestPart @NotNull @NotBlank
                                           @Parameter(schema = @Schema(type = "string", format = "string")) String deploymentName,
                                       @RequestPart MultipartFile[] files,
                                       HttpServletResponse response) {
        var resources = new LinkedHashMap<String, InputStream>();
        try {
            for (MultipartFile file : files) {
                resources.put(deploymentName + '/' + file.getOriginalFilename(), file.getInputStream());
            }
            var traceId = TraceUtils.toParent();
            response.setHeader(TRACE_ID_HEADER, traceId.getValue());
            return RestResponse.success(processManagerService.deploy(deploymentName, resources));
        } catch (Exception e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), "Deployment initialization failed", deploymentName, e);
        } finally {
            for (Map.Entry<String, InputStream> resource : resources.entrySet()) {
                try {
                    resource.getValue().close();
                } catch (IOException ex) {
                    // todo naming service
                    log.warn("Resources was not closed correctly: {}", resource.getKey(), ex);
                }
            }
        }
    }

    @Operation(summary = "Undeploy deployment by id",
            tags = {"deployment"})
    @DeleteMapping(
            path = "/deployments/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<List<DeploymentInfo>> undeploy(@PathVariable("id") @NotNull @NotBlank String id) {
        try {
            return RestResponse.success(processManagerService.undeploy(id, null));
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), PROCESS_UNDEPLOYMENT_FAILED, id, e);
        }
    }

    @Operation(summary = "Undeploy deployment by name",
            tags = {"deployment"})
    @DeleteMapping(
            path = "/deployments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<List<DeploymentInfo>> undeployByName(@RequestParam("name") @NotNull @NotBlank String name) {
        try {
            return RestResponse.success(processManagerService.undeploy(null, name));
        } catch (InitializationException e) {
            throw new DeploymentException(ErrorCodes.PROCESSING_FAILED.name(), PROCESS_UNDEPLOYMENT_FAILED, name, e);
        }
    }

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
    @PostMapping(value = "/files/{fileUuid}/deploy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
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
    @PostMapping(value = "/files/deploy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("isTalentAdmin() or isProcessManager() or isAdmin()")
    public RestResponse<String> deployByPathAndFilename(@RequestParam("path") String path,
                                                        @RequestParam("file-name") String fileName) {

        return success(deploymentService.deploy(path, fileName));
    }
}
