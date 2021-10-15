package com.tesco.pma.process.model;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.tesco.pma.process.api.PMProcessMetadata;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 15:45
 */
public class TimelineParser {

    public void parseTimeline(PMProcessMetadata metadata, Collection<Task> tasks) {
        tasks.forEach(task -> processTask(metadata, task));
    }

    private void processTask(PMProcessMetadata metadata, Task task) {
        var extensionElements = task.getExtensionElements();
        if (extensionElements != null) {
            var query = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
            if (query.count() < 1) {
                return;
            }
            CamundaProperties props = query.singleResult();
            String timelinePoint = null;
            String timelinePointName = null;

            for (CamundaProperty prop : props.getCamundaProperties()) {
                if (prop.getCamundaName().equals(PMProcessMetadata.TIMELINE_POINT)) {
                    timelinePoint = prop.getCamundaValue();
                } else if (prop.getCamundaName().equals(PMProcessMetadata.TIMELINE_POINT_NAME)) {
                    timelinePointName = prop.getCamundaValue();
                }
            }
            if (timelinePoint != null) {
                metadata.getTimeline().put(timelinePoint, timelinePointName != null ? timelinePointName : timelinePoint);
            }
        }
    }
}
