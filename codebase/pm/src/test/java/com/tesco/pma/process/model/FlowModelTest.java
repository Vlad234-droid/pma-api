package com.tesco.pma.process.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.process.api.PMProcessMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 11:10
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FlowModelTest extends AbstractCamundaSpringBootTest {
    private static final String PROCESS_NAME = "GROUPS_HO_S_WL1";
    private static final String FORMS_PATH = "/com/tesco/pma/flow/forms/";
    private static final String FORM_1 = "pm_o_1.form";
    private static final String FORM_2 = "pm_o_2.form";

    private final ResourceProvider resourceProvider = new FormsResourceProvider();

    private static class FormsResourceProvider implements ResourceProvider {
        @Override
        public InputStream read(String resourceName) throws IOException {
            return getClass().getResourceAsStream(FORMS_PATH + resourceName);
        }

        @Override
        public String resourceToString(final String resourceName) throws IOException {
            try (InputStream is = getClass().getResourceAsStream(FORMS_PATH + resourceName)) {
                return IOUtils.toString(is);
            }
        }
    }

    @Test
    void readModel() {
        var processDefinition = getProcessDefinition();
        var model = getModel(processDefinition);
        assertNotNull(model);

        var metadata = new PMProcessMetadata();
        var tasks = model.getModelElementsByType(Task.class);

        var parser = new PMProcessModelParser(resourceProvider);
        parser.parse(metadata, tasks);

        assertEquals(3, metadata.getElements().size());
    }

    @Test
    void getFormName() {
        var parser = new PMProcessModelParser(resourceProvider);
        assertEquals(FORM_1, parser.getFormName("camunda-forms:deployment:forms/pm_o_1.form"));
        assertEquals(FORM_2, parser.getFormName("camunda-forms:deployment:pm_o_2.form"));
    }

    private BpmnModelInstance getModel(ProcessDefinition processDefinition) {
        return processEngine.getRepositoryService().getBpmnModelInstance(processDefinition.getId());
    }

    private ProcessDefinition getProcessDefinition() {
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionKey(PROCESS_NAME);
        return processDefinitionQuery.singleResult();
    }

    private List<Deployment> getDeployment(String deploymentId) {
        var dq= processEngine.getRepositoryService().createDeploymentQuery();
        dq.deploymentId(deploymentId);
        return dq.list();
    }
}
