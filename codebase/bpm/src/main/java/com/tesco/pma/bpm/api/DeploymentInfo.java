package com.tesco.pma.bpm.api;

import java.util.Date;

public interface DeploymentInfo {
    String getId();

    String getName();

    Date getDeploymentTime();

    String getSource();

    String getTenantId();
}
