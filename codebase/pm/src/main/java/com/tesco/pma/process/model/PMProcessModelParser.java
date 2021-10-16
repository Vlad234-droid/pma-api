package com.tesco.pma.process.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.model.PMElement;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 15:45
 */
public class PMProcessModelParser {
    private static final String PREFIX = "pm_";
    private static final String PM_TYPE = PREFIX + "type";
    private static final String PM_REVIEW = PREFIX + "review";
    private static final String PM_REVIEW_TYPE = PREFIX + "review_type";
    private static final String PM_REVIEW_MIN = PREFIX + "review_min";
    private static final String PM_REVIEW_MAX = PREFIX + "review_max";
    private static final String DEFAULT_PM_REVIEW_MIN = "1";
    private static final String DEFAULT_PM_REVIEW_MAX = "1";

    public void parse(PMProcessMetadata metadata, Collection<Task> tasks) {
        tasks.forEach(task -> processTask(metadata, task));
    }

    private void processTask(PMProcessMetadata metadata, Task task) {
        var extensionElements = task.getExtensionElements();
        if (extensionElements != null) {
            var propsQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
            if (propsQuery.count() < 1) {
                return;
            }
            CamundaProperties props = propsQuery.singleResult();
            Optional<CamundaProperty> typeOptional = props.getCamundaProperties().stream().filter(p ->
                    p.getCamundaName().equalsIgnoreCase(PM_TYPE) && !StringUtils.isBlank(p.getCamundaValue())
            ).findFirst();
            if (typeOptional.isPresent()) {
                var type = new GeneralDictionaryItem();
                type.setCode(typeOptional.get().getCamundaValue().toLowerCase());

                var pmElement = new PMElement(task.getId(), task.getName(), task.getName(), type);
                if (PM_REVIEW.equals(type.getCode())) {
                    parseReview(pmElement, props);
                }
                metadata.getElements().add(pmElement);
            }
        }
    }

    private void parseReview(PMElement pmElement, CamundaProperties props) {
        props.getCamundaProperties().forEach(property ->
                Arrays.asList(PM_REVIEW_TYPE, PM_REVIEW_MIN, PM_REVIEW_MAX)
                        .forEach(key -> {
                            if (property.getCamundaName().equalsIgnoreCase(key)) {
                                pmElement.getProperties().put(key, property.getCamundaValue());
                            }
                        }));

        pmElement.getProperties().putIfAbsent(PM_REVIEW_MIN, DEFAULT_PM_REVIEW_MIN);
        pmElement.getProperties().put(PM_REVIEW_MAX, DEFAULT_PM_REVIEW_MAX);
    }
}