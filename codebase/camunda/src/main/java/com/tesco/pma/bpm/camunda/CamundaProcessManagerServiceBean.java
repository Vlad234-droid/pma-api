package com.tesco.pma.bpm.camunda;

import com.tesco.pma.bpm.api.DeploymentInfo;
import com.tesco.pma.bpm.api.DeploymentInfoEntity;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.exception.InitializationException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.ResourceDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Camunda based implementation of {@link ProcessManagerService}
 */

@Slf4j
public class CamundaProcessManagerServiceBean implements ProcessManagerService {

    @Override
    public DeploymentInfo deployProcessArchive(String path) throws FileNotFoundException, InitializationException {
        return deployProcessArchive(new File(path));
    }

    @Override
    public DeploymentInfo deployProcessArchive(File resource) throws FileNotFoundException, InitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Deploy process from - " + resource);
        }
        try (var zipCheck = new ZipFile(resource)) {
            DeploymentBuilder builder = getProcessEngine().getRepositoryService().createDeployment();
            builder.name(resource.getName());
            builder.addZipInputStream(new ZipInputStream(new FileInputStream(resource))); //NOPMD
            return new DeploymentInfoEntity(builder.deploy());
        } catch (Exception e) {
            throw new InitializationException("Deploy process failed using " + resource, e);
        }
    }

    @Override
    public DeploymentInfo deploy(String name, Map<String, InputStream> resources)
            throws InitializationException {
        if (resources == null) {
            throw new InitializationException("Deploying resources were not passed");
        }
        if (log.isDebugEnabled()) {
            log.debug("Deploy process resources [{}]: {}", resources.size(), resources.keySet());
        }
        try {
            DeploymentBuilder builder = getProcessEngine().getRepositoryService().createDeployment();
            builder.name(name);
            for (Map.Entry<String, InputStream> resource : resources.entrySet()) {
                builder.addInputStream(resource.getKey(), resource.getValue());
            }
            return new DeploymentInfoEntity(builder.deploy());
        } catch (Exception e) {
            throw new InitializationException("Deployment " + name + " failed using " + resources.keySet(), e);
        }
    }

    @Override
    public List<DeploymentInfo> undeploy(String id, String name) throws InitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Undeploy deployed pack by name " + name);
        }
        try {
            var processEngine = getProcessEngine();
            var deploymentQuery = processEngine.getRepositoryService().createDeploymentQuery();
            if (id != null && !id.isBlank()) {
                deploymentQuery.deploymentId(id);
            }
            if (name != null && !name.isBlank()) {
                deploymentQuery.deploymentNameLike(name);
            }
            var result = new ArrayList<DeploymentInfo>();
            for (Deployment deployment : deploymentQuery.list()) {
                processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
                result.add(new DeploymentInfoEntity(deployment));
            }
            return result;
        } catch (Exception e) {
            throw new InitializationException("Deployed pack cannot be removed: " + name, e);
        }
    }

    @Override
    public void undeployProcess(String processName) throws InitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Undeploy process by name(key) " + processName);
        }
        try {
            var processEngine = getProcessEngine();
            var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionKey(processName);
            deleteProcessDeploymentsByQuery(processDefinitionQuery, processEngine);
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public void undeployProcess(String processName, String version) throws InitializationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Undeploy process by name(key) %s, version %s", processName, version));
            }
            var processEngine = getProcessEngine();
            var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionKey(processName);
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(version));
            deleteProcessDeploymentsByQuery(processDefinitionQuery, processEngine);
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public List<String> listProcesses() {
        var processEngine = getProcessEngine();
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        var list = processDefinitionQuery.orderByProcessDefinitionName().asc().list();
        return Objects.requireNonNullElse(list.stream().map(ResourceDefinition::getName).collect(Collectors.toList()),
                Collections.emptyList());
    }

    @Override
    public List<DeploymentInfo> listDeployments() {
        var processEngine = getProcessEngine();
        var deploymentQuery = processEngine.getRepositoryService().createDeploymentQuery();
        return deploymentQuery.list().stream()
                .map(DeploymentInfoEntity::new).collect(Collectors.toList());
    }

    @Override
    public List<String> getProcessesIds(String deploymentId, String resourceName) {
        var processEngine = getProcessEngine();
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.deploymentId(deploymentId).processDefinitionResourceName(resourceName);
        var list = processDefinitionQuery.orderByProcessDefinitionName().asc().list();
        return Objects.requireNonNullElse(list.stream().map(ResourceDefinition::getId).collect(Collectors.toList()),
                Collections.emptyList());
    }

    @Override
    public String runProcess(String processName, Map<String, ?> varMap) throws ProcessExecutionException {
        return runProcess(processName, null, varMap);
    }

    @Override
    public String runProcess(String processName) throws ProcessExecutionException {
        return runProcess(processName, null, null);
    }

    @Override
    public String runProcess(String processName, String version) throws ProcessExecutionException {
        return runProcess(processName, version, null);
    }

    @Override
    public String runProcess(String processName, String version, Map<String, ?> varMap) throws ProcessExecutionException {
        try {
            var processEngine = getProcessEngine();
            var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionKey(processName);

            ProcessDefinition processDefinition;
            if (version != null) {
                processDefinitionQuery.processDefinitionVersion(Integer.valueOf(version));
                processDefinition = processDefinitionQuery.singleResult();
            } else {
                processDefinition = processDefinitionQuery.latestVersion().singleResult();
            }

            if (processDefinition == null) {
                var message = "Couldn't find process by key %s, version %s";
                throw new ProcessExecutionException(String.format(message, processName, version));
            }
            var runtimeService = processEngine.getRuntimeService();
            String instanceId;
            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;

                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId(), castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
            }
            if (instanceId == null) {
                var message = "Couldn't start process by key %s, version %s";
                throw new ProcessExecutionException(String.format(message, processName, version));
            }
            return instanceId;
        } catch (Exception e) {
            throw new ProcessExecutionException(e);
        }
    }

    @Override
    public String runProcessByResourceName(String resourceName, Map<String, ?> varMap) throws ProcessExecutionException {
        try {
            var processEngine = getProcessEngine();
            var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionResourceName(resourceName).latestVersion();
            var processDefinition = processDefinitionQuery.singleResult();
            if (processDefinition == null) {
                var message = "Couldn't find process by resource name %s";
                throw new ProcessExecutionException(String.format(message, resourceName));
            }
            var runtimeService = processEngine.getRuntimeService();
            String instanceId;
            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;

                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId(), castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
            }
            if (instanceId == null) {
                var message = "Couldn't find process by resource name %s";
                throw new ProcessExecutionException(String.format(message, resourceName));
            }
            return instanceId;
        } catch (Exception e) {
            throw new ProcessExecutionException(e);
        }
    }

    @Override
    public String runProcessById(String processId, Map<String, ?> varMap) throws ProcessExecutionException {
        try {
            var processEngine = getProcessEngine();
            var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionId(processId);

            var processDefinition = processDefinitionQuery.singleResult();
            if (processDefinition == null) {
                var message = "Couldn't find process by id %s";
                throw new ProcessExecutionException(String.format(message, processId));
            }
            var runtimeService = processEngine.getRuntimeService();
            String instanceId;
            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;

                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId(), castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
            }
            if (instanceId == null) {
                var message = "Couldn't start process by id %s";
                throw new ProcessExecutionException(String.format(message, processId));
            }
            return instanceId;
        } catch (Exception e) {
            throw new ProcessExecutionException(e);
        }
    }

    @Override
    public String runProcessByEvent(String eventName, Map<String, ?> varMap) throws ProcessExecutionException {
        String instanceId;
        try {
            var processEngine = getProcessEngine();
            var runtimeService = processEngine.getRuntimeService();

            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;
                instanceId = runtimeService.startProcessInstanceByMessage(eventName, castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceByMessage(eventName).getId();
            }
            if (instanceId == null) {
                var message = "Couldn't start process by key %s";
                throw new ProcessExecutionException(String.format(message, eventName));
            }
        } catch (Exception e) {
            throw new ProcessExecutionException(e);
        }
        return instanceId;
    }

    @Override
    public String runProcessByEvent(String eventName) throws ProcessExecutionException {
        return this.runProcessByEvent(eventName, null);
    }

    @Override
    public void signalEvent(String code, String instanceId) throws ProcessExecutionException {
        signalEvent(code, instanceId, null);
    }

    @Override
    public void signalEvent(String code, String instanceId, Map<String, Object> processVariables) throws ProcessExecutionException {
        try {
            var processEngine = getProcessEngine();
            var runtimeService = processEngine.getRuntimeService();
            var executionQuery = runtimeService.createExecutionQuery();
            executionQuery.processInstanceId(instanceId);
            executionQuery.messageEventSubscriptionName(code);
            var execution = executionQuery.singleResult();
            if (execution == null) {
                var message = "Couldn't find execution by event code %s for process instance %s";
                throw new ProcessExecutionException(String.format(message, code, instanceId));
            }
            if (processVariables != null) {
                runtimeService.messageEventReceived(code, execution.getId(), processVariables);
            } else {
                runtimeService.messageEventReceived(code, execution.getId());
            }
        } catch (Exception e) {
            throw new ProcessExecutionException("Process execution failed", e);
        }
    }

    public ProcessEngine getProcessEngine() {
        return ProcessEngines.getDefaultProcessEngine();
    }

    private void deleteProcessDeploymentsByQuery(ProcessDefinitionQuery processDefinitionQuery, ProcessEngine processEngine) {
        List<ProcessDefinition> definitions = processDefinitionQuery.list();
        Set<String> deployments = new HashSet<>();
        for (ProcessDefinition definition : definitions) {
            deployments.add(definition.getDeploymentId());
        }
        for (String deploymentId : deployments) {
            processEngine.getRepositoryService().deleteDeployment(deploymentId, true);
        }
    }
}
