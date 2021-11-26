package com.tesco.pma.process.service;

import com.tesco.pma.cycle.model.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class FormsResourceProvider implements ResourceProvider {

    private static final String FORMS_PATH = "/com/tesco/pma/flow/";

    @Override
    public InputStream read(String resourceName) throws IOException {
        return getClass().getResourceAsStream(FORMS_PATH + resourceName);
    }

    @Override
    public String resourceToString(final String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(FORMS_PATH + resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
