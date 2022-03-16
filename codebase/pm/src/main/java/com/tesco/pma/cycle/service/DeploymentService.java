package com.tesco.pma.cycle.service;

import java.util.UUID;

/**
 * Deployment service - deploy files into camunda
 */
public interface DeploymentService {
    /**
     * Deploy file resource by uuid
     *
     * @param fileUuid file uuid
     * @return id of the deployed process definition
     */
    String deploy(UUID fileUuid);

    /**
     * Deploy file resource by path and name
     *
     * @param path file path
     * @param fileName file name
     * @return id of the deployed process definition
     */
    String deploy(String path, String fileName);
}
