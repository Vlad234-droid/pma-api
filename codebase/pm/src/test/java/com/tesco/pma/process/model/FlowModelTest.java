package com.tesco.pma.process.model;

import java.util.Collection;

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
    public static final String PROCESS_NAME = "GROUPS_HO_S_WL1";

    @Test
    void timelineModel() {
        BpmnModelInstance model = getModel();
        assertNotNull(model);

        PMProcessMetadata metadata = new PMProcessMetadata();
        Collection<Task> tasks = model.getModelElementsByType(Task.class);

        new TimelineParser().parseTimeline(metadata, tasks);

        assertEquals(2, metadata.getTimeline().size());

    }

    private BpmnModelInstance getModel() {
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionKey(PROCESS_NAME);
        var processDefinition = processDefinitionQuery.singleResult();
        return processEngine.getRepositoryService().getBpmnModelInstance(processDefinition.getId());
    }

}
