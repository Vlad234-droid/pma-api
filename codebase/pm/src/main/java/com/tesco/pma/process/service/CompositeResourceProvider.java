package com.tesco.pma.process.service;

import com.tesco.pma.cycle.api.model.ResourceProvider;
import com.tesco.pma.file.api.File;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Primary
@AllArgsConstructor
@Slf4j
public class CompositeResourceProvider implements ResourceProvider {

    private final ClasspathResourceProvider classpathResourceProvider;
    private final FsResourceProvider fsResourceProvider;

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {
        try {
            return fsResourceProvider.read(resourcePath, resourceName);

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

    @Override
    public File readFile(String resourcePath, String resourceName) {
        return fsResourceProvider.readFile(resourcePath, resourceName);
    }

    @Override
    public File readFile(UUID uuid) {
        return fsResourceProvider.readFile(uuid);
    }
}
