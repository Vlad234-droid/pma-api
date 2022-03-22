package com.tesco.pma.bpm.camunda;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.exception.InitializationException;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CamundaProcessManagerServiceTest {

    private static final String UNEXISTED = "unexisted";
    private static final String PROCESS_NAME = "activityTest";
    private static final String PROCESS_BPMN20_XML = "process.bpmn20.xml";
    private static final String SAME_MESSAGE_BPMN20_XML = "sameMessage.bpmn20.xml";
    private static final String OTHER_SAME_MESSAGE_BPMN20_XML = "otherSameMessage.bpmn20.xml";
    private static final String ACTIVITY_TEST_BAR = "activityTest.bar";
    private static final String PROCESS_ARCHIVE_PATH =
            System.getProperty("java.io.tmpdir") + "/" + ACTIVITY_TEST_BAR + System.currentTimeMillis();

    private static RepositoryService repositoryService;
    private static RuntimeService runtimeService;
    private static CamundaProcessManagerServiceBean processManager;

    private String deploymentId;

    @BeforeAll
    public static void setup() throws FileNotFoundException, IOException {
        ProcessEngineConfiguration processEngineConfiguration =
                ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        processManager = new CamundaProcessManagerServiceBean();
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();

        createProcessArchive();
    }

    private static void createProcessArchive() throws IOException {
        InputStream input = CamundaProcessManagerServiceTest.class.getResourceAsStream("/" + PROCESS_BPMN20_XML);
        File zip = new File(PROCESS_ARCHIVE_PATH);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip)); //NOPMD
        ZipEntry entry = new ZipEntry(PROCESS_BPMN20_XML);
        try {
            out.putNextEntry(entry);
            IOUtils.copy(input, out);
            out.closeEntry();
        } finally {
            out.close();
            input.close();
        }
    }

    @AfterEach
    public void deleteDeployment() {
        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @BeforeEach
    public void deployProcess() {
        deploymentId = repositoryService.createDeployment().addClasspathResource(PROCESS_BPMN20_XML).deploy().getId();
    }

    @AfterAll
    public static void cleanUp() {
        File zip = new File(PROCESS_ARCHIVE_PATH);
        zip.delete();
    }

    @Test
    void deployProcessTest() throws FileNotFoundException, InitializationException {
        deploymentId = processManager.deployProcessArchive(new File(PROCESS_ARCHIVE_PATH)).getId();
        long deploymentCount = repositoryService.createDeploymentQuery().deploymentId(deploymentId).count();
        assertEquals(1, deploymentCount, "Should be 1 deployed process");
    }

    @Test
    void undeployProcessByKeyTest() throws InitializationException {
        deployProcess();
        long deploymentCount = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_NAME).count();
        assertEquals(2, deploymentCount, "Should be 2 deployed process");
        processManager.undeployProcess(PROCESS_NAME);
        deploymentCount = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_NAME).count();
        assertEquals(0, deploymentCount, "Should be 0 deployed process");
    }

    @Test
    void undeployProcessByKeyVersionTest() throws InitializationException {
        deployProcess();
        long deploymentCount = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_NAME).count();
        assertEquals(2, deploymentCount, "Should be 2 deployed process");
        processManager.undeployProcess(PROCESS_NAME, "1");
        deploymentCount = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_NAME).count();
        assertEquals(1, deploymentCount, "Should be 1 deployed process");
    }

    @Test
    void runProcessByKeyTest() throws ProcessExecutionException {
        String executionId = processManager.runProcess(PROCESS_NAME);
        Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        assertNotNull(execution, "Execution should be present");
        assertFalse(execution.isEnded(), "Execution should not be ended");
    }

    @Test
    void runProcessByKeyVersionTest() throws ProcessExecutionException {
        deployProcess();
        final int version = 1;
        String executionId = processManager.runProcess(PROCESS_NAME, String.valueOf(version));

        Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();

        assertNotNull(execution, "Execution should be present");
        assertFalse(execution.isEnded(), "Execution should not be ended");
        assertEquals(version, processDefinition.getVersion(), String.format("Process definition should be %s version", version));
    }

    @Test
    void signalProcessByEventTest() throws ProcessExecutionException {
        String processInstanceId = processManager.runProcess(PROCESS_NAME);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        assertNotNull(processInstance, "Process instance should be present");
        processManager.signalEvent("auth", processInstanceId);
        processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        assertNull(processInstance, "Process instance should not be present");
    }

    @Test
    void testSameMessageNameFails() {
        String deploymentId = repositoryService.createDeployment().addClasspathResource(SAME_MESSAGE_BPMN20_XML).deploy().getId();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addClasspathResource(OTHER_SAME_MESSAGE_BPMN20_XML);
        try {
            deploymentBuilder.deploy();
            fail("exception expected");
        } catch (ProcessEngineException e) {
            e.printStackTrace(); //NOPMD
            assertTrue(e.getMessage().contains("there already is a message event subscription for the message with name"));
        }

        // clean db:
        repositoryService.deleteDeployment(deploymentId);
    }

    @Test
    void runUnexistedProcessByKey() {
        assertThrows(ProcessExecutionException.class, () -> processManager.runProcess(UNEXISTED));
    }
}
