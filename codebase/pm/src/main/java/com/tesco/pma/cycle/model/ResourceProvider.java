package com.tesco.pma.cycle.model;

import com.tesco.pma.fs.domain.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 17.10.2021 Time: 22:54
 */
public interface ResourceProvider {

    /**
     * Read resource by path and name
     *
     * @param resourcePath - resource path
     * @param resourceName - resource name
     * @return InputStream of the resource
     * @throws IOException
     */
    InputStream read(String resourcePath, String resourceName) throws IOException;

    /**
     * Read resource by path and name
     *
     * @param resourcePath - resource path
     * @param resourceName - resource name
     * @return String content of the resource
     * @throws IOException
     */
    String resourceToString(String resourcePath, String resourceName) throws IOException;

    File readFile(String resourcePath, String resourceName) throws IOException;

    File readFile(UUID uuid) throws IOException;

    /**
     * Read resource uuid by path and name
     *
     * @param resourcePath - resource path
     * @param resourceName - resource name
     * @return uuid of the resource
     * @throws IOException
     */
    UUID readFileUuid(String resourcePath, String resourceName);
}
