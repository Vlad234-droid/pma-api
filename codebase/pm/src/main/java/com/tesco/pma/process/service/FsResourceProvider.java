package com.tesco.pma.process.service;

import com.tesco.pma.cycle.model.ResourceProvider;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class FsResourceProvider implements ResourceProvider {

    private final FileService fileService;

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {

        return new ByteArrayInputStream(
                fileService.get(resourcePath, resourceName, true).getFileContent());
    }

    @Override
    public String resourceToString(String resourcePath, String resourceName) throws IOException {
        try (InputStream is = this.read(resourcePath, resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    @Override
    public File readFile(String resourcePath, String resourceName) {
        return fileService.get(resourcePath, resourceName, true);
    }

    @Override
    public File readFile(UUID uuid) {
        return fileService.get(uuid, true);
    }

    @Override
    public UUID readFileUuid(String resourcePath, String resourceName) {
        return this.readFile(resourcePath, resourceName).getUuid();
    }
}
