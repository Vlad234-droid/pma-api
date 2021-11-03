package com.tesco.pma.process.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.model.PMCycleElement;

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
    private static final String PROCESS_NAME_2 = "GROUP_HO_S_WL1";
    private static final String FORMS_PATH = "/com/tesco/pma/flow/";
    private static final String FORM_1 = "forms/pm_o_1.form";
    private static final String FORM_2 = "pm_o_2.form";

    private final ResourceProvider resourceProvider = new FormsResourceProvider();
    private final PMProcessModelParser parser = new PMProcessModelParser(resourceProvider);

    private static class FormsResourceProvider implements ResourceProvider {
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

    @Test
    void readModel() {
        checkModel(PROCESS_NAME);
    }

    @Test
    void readModel2() {
        checkModel(PROCESS_NAME_2);
    }

    @Test
    void getFormName() {
        assertEquals(FORM_1, parser.getFormName("camunda-forms:deployment:forms/pm_o_1.form"));
        assertEquals(FORM_2, parser.getFormName("camunda-forms:deployment:pm_o_2.form"));
    }

    @Test
    void unwrap() {
        Assertions.assertNull(PMProcessModelParser.unwrap(null));
        Assertions.assertNull(PMProcessModelParser.unwrap(""));
        Assertions.assertNull(PMProcessModelParser.unwrap(" "));

        var expected = "Set objectives";
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n objectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap(" Set  objectives "));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\nobjectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n\robjectives"));
        Assertions.assertEquals(expected, PMProcessModelParser.unwrap("Set\n\r\n\r objectives"));
    }

    @Test
    void defaultValue() {
        var expected = "Set objectives";
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue(null, expected));
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue("  ", expected));
        Assertions.assertEquals(expected, PMProcessModelParser.defaultValue("Set objectives ", ""));
    }

    private void checkModel(String processName) {
        var processDefinition = getProcessDefinition(processName);
        var model = getModel(processDefinition);
        assertNotNull(model);

        var metadata = new PMProcessMetadata();
        var cycle = new PMCycleElement();
        cycle.setCode(processDefinition.getName());
        metadata.setCycle(cycle);

        var tasks = model.getModelElementsByType(Activity.class);
        parser.parse(cycle, tasks);

        assertEquals(3, cycle.getReviews().size());
    }

    private BpmnModelInstance getModel(ProcessDefinition processDefinition) {
        return processEngine.getRepositoryService().getBpmnModelInstance(processDefinition.getId());
    }

    private ProcessDefinition getProcessDefinition(String processName) {
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionKey(processName);
        return processDefinitionQuery.singleResult();
    }

    private List<Deployment> getDeployment(String deploymentId) {
        var dq= processEngine.getRepositoryService().createDeploymentQuery();
        dq.deploymentId(deploymentId);
        return dq.list();
    }
}
