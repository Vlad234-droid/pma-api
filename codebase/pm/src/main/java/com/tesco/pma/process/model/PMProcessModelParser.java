package com.tesco.pma.process.model;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.process.api.model.PMCycle;
import com.tesco.pma.process.api.model.PMForm;
import com.tesco.pma.process.api.model.PMReview;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.tesco.pma.process.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.process.api.model.PMReview.DEFAULT_PM_REVIEW_MAX;
import static com.tesco.pma.process.api.model.PMReview.DEFAULT_PM_REVIEW_MIN;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_MAX;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_MIN;
import static com.tesco.pma.process.api.model.PMReview.PM_REVIEW_TYPE;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 15:45
 */
@Slf4j
@AllArgsConstructor
public class PMProcessModelParser {
    private static final Pattern FORM_NAME_PATTERN = Pattern.compile("(([\\w-_]+)\\.(form|json))$");

    private ResourceProvider resourceProvider;

    public void parse(PMCycle cycle, Collection<Task> tasks) {
        tasks.forEach(task -> processTask(cycle, task));
    }

    private void processTask(PMCycle cycle, Task task) {
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
                    cycle.getReviews().add(parseReview(task, props, type));
                }
            }
        }
    }

    private PMReview parseReview(Task task, CamundaProperties props, String type) {
        var pmReview = new PMReview(task.getId(), task.getName(), task.getName(), new GeneralDictionaryItem(null, type, null));

        props.getCamundaProperties().forEach(property -> {
            var key = property.getCamundaName().toLowerCase();
            if (PM_REVIEW_TYPE.equals(key)) {
                pmReview.setReviewType(new GeneralDictionaryItem(null, property.getCamundaValue(), null));
            }
            pmReview.getProperties().put(key, property.getCamundaValue());
        });
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MIN, DEFAULT_PM_REVIEW_MIN);
        pmReview.getProperties().put(PM_REVIEW_MAX, DEFAULT_PM_REVIEW_MAX);

        if (task instanceof UserTask) {
            var userTask = (UserTask) task;
            var formKey = userTask.getCamundaFormKey();
            try {
                var formName = getFormName(formKey);
                var formJson = resourceProvider.resourceToString(formName);

                pmReview.setForm(new PMForm(formKey, formName, formJson));
            } catch (Exception e) {
                log.warn("Form was not found {}", formKey, e); //todo exception handling
            }
        }
        return pmReview;
    }

    String getFormName(String key) {
        var matcher = FORM_NAME_PATTERN.matcher(key);
        return matcher.find() ? matcher.group(1) : null;
    }
}