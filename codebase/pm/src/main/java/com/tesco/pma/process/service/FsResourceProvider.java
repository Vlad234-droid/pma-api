package com.tesco.pma.process.service;

import com.tesco.pma.cycle.model.ResourceProvider;
import com.tesco.pma.fs.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Primary
@AllArgsConstructor
@Slf4j
public class FsResourceProvider implements ResourceProvider {

    private final FileService fileService;
    private final ResourceProvider classpathResourceProvider;

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {
        try {
            return new ByteArrayInputStream(
                    fileService.get(resourcePath, resourceName, true).getFileContent());

        } catch (Exception e) {
            log.warn("Resource {} not found in DB", resourceName, e);
            return classpathResourceProvider.read(resourcePath, resourceName);
        }
    }

    @Override
    public String resourceToString(String resourcePath, String resourceName) throws IOException {
        try (InputStream is = this.read(resourcePath, resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
