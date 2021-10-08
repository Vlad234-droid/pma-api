package com.tesco.pma.flow.configuration;

import java.util.List;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

import com.tesco.pma.service.deployment.ProcessMetadataService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 07.10.2021 Time: 22:03
 */
@Slf4j
public class ReviewUserTasksParseListener extends AbstractBpmnParseListener {

    ProcessMetadataService processMetadataService;

    public ReviewUserTasksParseListener(ProcessMetadataService processMetadataService) {
        this.processMetadataService = processMetadataService;
    }

    @Override
    public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
        timelineProcessing(taskElement, activity);
    }

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        timelineProcessing(userTaskElement, activity);
    }

    private void timelineProcessing(Element userTaskElement, ActivityImpl activity) {
        var extensionElement = userTaskElement.element("extensionElements");
        if (extensionElement != null) {
            // get the <camunda:properties ...> element from the service task
            var propertiesElement = extensionElement.element("properties");
            if (propertiesElement != null) {
                //  get list of <camunda:property ...> elements from the service task
                List<Element> propertyList = propertiesElement.elements("property");

                String timelinePoint = null;
                String timelinePointName = null;

                for (Element property : propertyList) {
                    // get the name and the value of the extension property element
                    var name = property.attribute("name");
                    var value = property.attribute("value");
                    // check if name attribute has the expected value
                    if ("timelinePoint".equals(name)) {
                        timelinePoint = value;
                        // add execution listener to the given service task element
                        // to execute it when the end event of the user task fired
                        var reviewStatusListener = new ReviewStatusListener(value);
                        activity.addListener(TaskListener.EVENTNAME_COMPLETE, reviewStatusListener);
                    } else if ("timelinePointName".equals(name)) {
                        timelinePointName = value;
                    }
                }
                if (timelinePoint != null) {
                    var processName = activity.getProcessDefinition().getName(); // todo: deploymentId
                    processMetadataService.addTimeline(processName, timelinePoint,
                            timelinePointName != null ? timelinePointName : timelinePoint);
                }
            }
        }
    }
}
