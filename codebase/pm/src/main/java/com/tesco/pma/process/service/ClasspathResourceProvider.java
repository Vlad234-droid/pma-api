package com.tesco.pma.process.service;

import com.tesco.pma.cycle.model.ResourceProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ClasspathResourceProvider implements ResourceProvider {

    @Override
    public InputStream read(String resourcePath, String resourceName) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(FilenameUtils.concat(resourcePath, resourceName));
    }

    @Override
    public String resourceToString(String resourcePath, String resourceName) throws IOException {
        try (InputStream is = read(resourcePath, resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
