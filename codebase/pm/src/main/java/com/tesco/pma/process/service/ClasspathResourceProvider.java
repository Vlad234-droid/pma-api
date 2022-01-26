package com.tesco.pma.process.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.util.ResourceProvider;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClasspathResourceProvider implements ResourceProvider {

    @Value("${camunda.bpm.deployment-resource-paths}")
    private String[] basePaths;

    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {

        var purePath = FilenameUtils.concat(resourcePath, resourceName);
        var basePathsToCheck = new ArrayList<String>();
        basePathsToCheck.add(StringUtils.EMPTY);
        basePathsToCheck.addAll(Arrays.asList(basePaths));

        for (String basePath : basePathsToCheck) {
            var path = FilenameUtils.concat(basePath, purePath);

            try {
                var result = getClass().getClassLoader().getResourceAsStream(path);

                if (result != null) {
                    return result;
                }

            } catch (Exception e) {
                log.warn(messageSourceAccessor.getMessage(
                        PMProcessErrorCodes.RESOURCE_NOT_FOUND, Map.of("path", path)));
            }
        }

        throw new NotFoundException(PMProcessErrorCodes.RESOURCE_NOT_FOUND.getCode(),
                messageSourceAccessor.getMessage(PMProcessErrorCodes.RESOURCE_NOT_FOUND, Map.of("path", purePath)));
    }

    @Override
    public String resourceToString(String resourcePath, String resourceName) throws IOException {
        try (InputStream is = read(resourcePath, resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    @SneakyThrows
    @Override
    public File readFile(String resourcePath, String resourceName) {
        File file = new File();
        file.setUuid(UUID.randomUUID());
        file.setFileContent(IOUtils.toByteArray(read(resourcePath, resourceName)));
        return file;
    }

    @Override
    public File readFile(UUID uuid) {
        throw new UnsupportedOperationException();
    }

}
