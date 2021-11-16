package com.tesco.pma.bpm.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.engine.repository.Deployment;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfoEntity implements DeploymentInfo, Deployment {
    private String id;
    private String name;
    private Date deploymentTime;
    private String source;
    private String tenantId;

    public DeploymentInfoEntity(Deployment deployment) {
        this.id = deployment.getId();
        this.name = deployment.getName();
        this.deploymentTime = deployment.getDeploymentTime();
        this.source = deployment.getSource();
        this.tenantId = getTenantId();
    }

    public DeploymentInfoEntity(DeploymentInfo deploymentInfo) {
        this.id = deploymentInfo.getId();
        this.name = deploymentInfo.getName();
        this.deploymentTime = deploymentInfo.getDeploymentTime();
        this.source = deploymentInfo.getSource();
        this.tenantId = getTenantId();
    }
}
