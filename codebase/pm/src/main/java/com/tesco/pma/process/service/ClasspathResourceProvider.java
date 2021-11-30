package com.tesco.pma.process.service;

import com.tesco.pma.cycle.model.ResourceProvider;
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

@Service
@Slf4j
public class ClasspathResourceProvider implements ResourceProvider {

    @Value("${camunda.bpm.default-forms-paths}")
    private String[] basePaths;

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {

        var purePath = FilenameUtils.concat(resourcePath, resourceName);
        var basePathsToCheck = new ArrayList<String>();
        basePathsToCheck.add(StringUtils.EMPTY);
        basePathsToCheck.addAll(Arrays.asList(basePaths));

        for (String basePath : basePathsToCheck) {
            var path = FilenameUtils.concat(basePath, purePath);

            try {
                return getClass().getClassLoader().getResourceAsStream(path);

            } catch (Exception e){
                log.warn("Error while trying to get resource {}", path, e);
            }
        }

        throw new NullPointerException(String.format("Resource not found %s %s", resourceName, resourcePath));
    }

    @Override
    public String resourceToString(String resourcePath, String resourceName) throws IOException {
        try (InputStream is = read(resourcePath, resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
