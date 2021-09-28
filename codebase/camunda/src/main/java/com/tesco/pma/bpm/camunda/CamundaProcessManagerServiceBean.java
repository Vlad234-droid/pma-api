package com.tesco.pma.bpm.camunda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

import com.tesco.pma.exception.InitializationException;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;

import lombok.extern.slf4j.Slf4j;


/**
 * Camunda based implementation of {@link ProcessManagerService}
 */

@Slf4j
public class CamundaProcessManagerServiceBean implements ProcessManagerService {

    @Override
    public String deployProcess(String path) throws FileNotFoundException, InitializationException {
        return deployProcess(new File(path));
    }

    @Override
    public String deployProcess(File resource) throws FileNotFoundException, InitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Deploy process from - " + resource);
        }
        try (ZipFile zipCheck = new ZipFile(resource)) {
            DeploymentBuilder builder = getProcessEngine().getRepositoryService().createDeployment();
            builder.name(resource.getName());
            builder.addZipInputStream(new ZipInputStream(new FileInputStream(resource))); //NOPMD
            return builder.deploy().getId();
        } catch (Exception e) {
            throw new InitializationException("Deploy process failed using " + resource, e);
        }
    }

    @Override
    public void undeployProcess(String processName) throws InitializationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Undeploy process by name(key) " + processName);
            }
            ProcessEngine processEngine = getProcessEngine();
            ProcessDefinitionQuery processDefinitionQuery;
            processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
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
            ProcessEngine processEngine = getProcessEngine();
            ProcessDefinitionQuery processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionKey(processName);
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(version));
            deleteProcessDeploymentsByQuery(processDefinitionQuery, processEngine);
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public List<String> listProcesses() {
        ProcessEngine processEngine = getProcessEngine();
        ProcessDefinitionQuery processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        var list = processDefinitionQuery.orderByProcessDefinitionName().asc().list();
        return Objects.requireNonNullElse(list.stream().map(ResourceDefinition::getName).collect(Collectors.toList()),
                Collections.emptyList());
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
            ProcessEngine processEngine = getProcessEngine();
            ProcessDefinitionQuery processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionKey(processName);
            if (version != null) {
                processDefinitionQuery.processDefinitionVersion(Integer.valueOf(version));
            }
            ProcessDefinition processDefinition = processDefinitionQuery.singleResult();
            if (processDefinition == null) {
                String message = "Couldn't find process by key %s, version %s";
                throw new ProcessExecutionException(String.format(message, processName, version));
            }
            RuntimeService runtimeService = processEngine.getRuntimeService();
            String instanceId;
            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;

                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId(), castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
            }
            if (instanceId == null) {
                String message = "Couldn't start process by key %s, version %s";
                throw new ProcessExecutionException(String.format(message, processName, version));
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
            ProcessEngine processEngine = getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();

            if (varMap != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMapVariables = (Map<String, Object>) varMap;
                instanceId = runtimeService.startProcessInstanceByMessage(eventName, castedMapVariables).getId();
            } else {
                instanceId = runtimeService.startProcessInstanceByMessage(eventName).getId();
            }
            if (instanceId == null) {
                String message = "Couldn't start process by key %s";
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
            ProcessEngine processEngine = getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            ExecutionQuery executionQuery = runtimeService.createExecutionQuery();
            executionQuery.processInstanceId(instanceId);
            executionQuery.messageEventSubscriptionName(code);
            Execution execution = executionQuery.singleResult();
            if (execution == null) {
                String message = "Couldn't find execution by event code %s for process instance %s";
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
}
