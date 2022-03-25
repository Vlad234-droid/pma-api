package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.api.DeploymentInfo;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.exception.PMCycleException;
import com.tesco.pma.exception.InitializationException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.fs.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.cycle.exception.ErrorCodes.FILE_DEPLOYMENT_ERROR;
import static java.util.Map.of;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {
    public static final String FILENAME_PARAMETER_NAME = "filename";

    private final FileService fileService;
    private final ProcessManagerService processManagerService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public String deploy(UUID fileUuid) {
        var file = fileService.get(fileUuid, true);
        return deployProcess(file);
    }

    @Override
    public String deploy(String path, String fileName) {
        var file = fileService.get(path, fileName, true, null);
        return deployProcess(file);
    }

    private String deployProcess(File file) {
        InputStream fileContent = new ByteArrayInputStream(file.getFileContent());

        var resourceName = file.getFileName();
        var processName = getBaseName(resourceName);

        DeploymentInfo deploymentInfo;
        try {
            deploymentInfo = processManagerService.deploy(processName,
                    of(resourceName, fileContent));

            log.debug("Deployment id: {}", deploymentInfo.getId());

            List<String> procdefs = processManagerService.getProcessesIds(deploymentInfo.getId(), resourceName);

            var deploymentResult = deploymentInfo.getId();
            if (!isEmpty(procdefs)) {
                deploymentResult = procdefs.iterator().next();
            }
            log.debug("Deployment result: {}", deploymentResult);
            return deploymentResult;
        } catch (InitializationException ex) {
            log.error("Exception while deploying file: {}", file.getFileName());
            throw new PMCycleException(FILE_DEPLOYMENT_ERROR.getCode(),
                    messageSourceAccessor.getMessage(FILE_DEPLOYMENT_ERROR.getCode(),
                            of(FILENAME_PARAMETER_NAME, file.getFileName())), null, ex);
        }
    }
}
