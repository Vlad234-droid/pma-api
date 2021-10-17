package com.tesco.pma.process.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.model.PMElement;
import com.tesco.pma.process.api.model.PMReview;

import lombok.extern.slf4j.Slf4j;

import static com.tesco.pma.process.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.process.api.model.PMReview.DEFAULT_PM_REVIEW_MAX;
import static com.tesco.pma.process.api.model.PMReview.DEFAULT_PM_REVIEW_MIN;
import static com.tesco.pma.process.api.model.PMReview.PM_FORM_JSON;
import static com.tesco.pma.process.api.model.PMReview.PM_FORM_KEY;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_MAX;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_MIN;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_TYPE;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 15:45
 */
@Slf4j
public class PMProcessModelParser {
    private static final Pattern FORM_NAME_PATTERN = Pattern.compile("([\\w_-|\\\\:]*)\\/*(([\\w-_]+)\\.(form|json))$");

    public void parse(PMProcessMetadata metadata, Collection<Task> tasks) {
        tasks.forEach(task -> processTask(metadata, task));
    }

    public void parseForms(PMProcessMetadata metadata, ResourceProvider resourceProvider) {
        metadata.getElements().stream().filter(pmElement -> pmElement.getProperties().containsKey(PM_FORM_KEY))
                .forEach(pmElement -> parseForm(pmElement, resourceProvider));
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
                var type = typeOptional.get().getCamundaValue().toLowerCase();
                if (PM_REVIEW.equals(type)) {
                    metadata.getElements().add(parseReview(task, props, type));
                }
            }
        }
    }

    private PMReview parseReview(Task task, CamundaProperties props, String type) {
        var pmReview = new PMReview(task.getId(), task.getName(), task.getName(), new GeneralDictionaryItem(null, type, null));
        props.getCamundaProperties().forEach(property ->
                Arrays.asList(PM_REVIEW_TYPE, PM_REVIEW_MIN, PM_REVIEW_MAX)
                        .forEach(key -> {
                            if (property.getCamundaName().equalsIgnoreCase(key)) {
                                pmReview.getProperties().put(key, property.getCamundaValue());
                            }
                        }));
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MIN, DEFAULT_PM_REVIEW_MIN);
        pmReview.getProperties().put(PM_REVIEW_MAX, DEFAULT_PM_REVIEW_MAX);

        if (task instanceof UserTask) {
            var userTask = (UserTask) task;
            var formKey = userTask.getCamundaFormKey();
            pmReview.getProperties().put(PM_FORM_KEY, formKey);
        }
        return pmReview;
    }

    private void parseForm(PMElement pmElement, ResourceProvider resourceProvider) {
        var formKey = pmElement.getProperties().get(PM_FORM_KEY);
        try {
            var formName = getFormName(formKey);
            pmElement.getProperties().put(PM_FORM_JSON, resourceProvider.resourceToString(formName));
        } catch (Exception e) {
            log.warn("Form was not found {}", formKey, e); //todo exception handling
        }
    }

    private String getFormName(String key) {
        var matcher = FORM_NAME_PATTERN.matcher(key);
        return matcher.find() ? matcher.group(2) : null;
    }
}