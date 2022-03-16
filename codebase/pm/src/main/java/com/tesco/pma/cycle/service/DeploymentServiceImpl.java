package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.api.DeploymentInfo;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.exception.InitializationException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.fs.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.getBaseName;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {
    private final FileService fileService;
    private final ProcessManagerService processManagerService;

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
                    Map.of(resourceName, fileContent));

            log.debug("Deployment id: {}", deploymentInfo.getId());

            List<String> procdefs = processManagerService.getProcessesIds(deploymentInfo.getId(), resourceName);
            return procdefs.iterator().next();
        } catch (InitializationException e) {
            log.error("Exception while deploying file: {}", file.getFileName());
            //TODO throw exc
            return null;
        }
    }
}
