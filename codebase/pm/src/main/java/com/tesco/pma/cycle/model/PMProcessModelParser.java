package com.tesco.pma.cycle.model;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.api.model.PMFormElement;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.api.model.PMTimelinePointElement;
import com.tesco.pma.cycle.exception.ParseException;
import com.tesco.pma.error.ErrorCodeAware;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.tesco.pma.cycle.api.model.PMCycleElement.PM_CYCLE_TYPE;
import static com.tesco.pma.cycle.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.cycle.api.model.PMFormElement.PM_FORM_KEY;
import static com.tesco.pma.cycle.api.model.PMReviewElement.DEFAULT_PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.DEFAULT_PM_REVIEW_MIN;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_TYPE;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_PARSE_IS_BLANK;
import static com.tesco.pma.cycle.exception.ErrorCodes.PM_PARSE_NOT_FOUND;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.10.2021 Time: 15:45
 */
@Slf4j
@AllArgsConstructor
public class PMProcessModelParser {
    private static final Pattern FORM_NAME_PATTERN = Pattern.compile("(([\\w\\-_/]+)\\.(form|json))$");
    public static final String KEY = "key";
    public static final String VALUE = "value";

    private final ResourceProvider resourceProvider;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    /**
     * Parse the model and return the metadata
     *
     * @param model Parsing model
     * @return cycle metadata
     * @throws ParseException any exceptions
     */
    public PMCycleMetadata parse(BpmnModelInstance model) {
        var processes = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);
        if (processes == null || processes.isEmpty()) {
            throw parseException(PM_PARSE_NOT_FOUND, Map.of(KEY, "process", VALUE, "absent"), "process", null);
        }
        var process = processes.iterator().next();
        PMCycleElement cycle = parseCycle(process);
        var metadata = new PMCycleMetadata(cycle);
        model.getModelElementsByType(Activity.class).forEach(task -> processTask(cycle, task));
        return metadata;
    }

    private PMCycleElement parseCycle(org.camunda.bpm.model.bpmn.instance.Process process) {
        var cycle = new PMCycleElement();
        cycle.setId(process.getId());
        cycle.setCode(process.getId());
        var props = getCamundaProperties(process);

        props.forEach(property -> {
            var key = property.getCamundaName().toLowerCase();
            cycle.getProperties().put(key, property.getCamundaValue());
            if (PM_CYCLE_TYPE.equals(key)) {
                cycle.setCycleType(getEnum(PMCycleType.class, key, property.getCamundaValue()));
            }
        });
        return cycle;
    }

    private Collection<CamundaProperty> getCamundaProperties(BaseElement element) {
        var extensionElements = element.getExtensionElements();
        if (extensionElements != null) {
            var propsQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
            if (propsQuery.count() > 0) {
                return propsQuery.singleResult().getCamundaProperties();
            }
        }
        return Collections.emptyList();
    }

    private void processTask(PMCycleElement cycle, Activity task) {
        var props = getCamundaProperties(task);
        if (props.isEmpty()) {
            return;
        }
        Optional<CamundaProperty> typeOptional = props.stream().filter(p ->
                p.getCamundaName().equalsIgnoreCase(PM_TYPE) && !StringUtils.isBlank(p.getCamundaValue())
        ).findFirst();
        if (typeOptional.isPresent()) {
            var type = typeOptional.get().getCamundaValue().toLowerCase();
            if (PM_REVIEW.equals(type)) {
                var review = parseReview(task, props);
                cycle.getTimelinePoints().add(review);
            } else if (PM_TIMELINE_POINT.equals(type)) {
                cycle.getTimelinePoints().add(parseTimelinePoint(task, props));
            }
        }
    }

    private PMTimelinePointElement parseTimelinePoint(Activity task, Collection<CamundaProperty> props) {
        var name = defaultValue(unwrap(task.getName()), task.getId());
        var pmTimelinePoint = new PMTimelinePointElement(task.getId(), name, name);
        props.forEach(property -> {
            var key = property.getCamundaName().toLowerCase();
            pmTimelinePoint.getProperties().put(key, property.getCamundaValue());
        });
        return pmTimelinePoint;
    }

    private PMReviewElement parseReview(Activity task, Collection<CamundaProperty> props) {
        var name = defaultValue(unwrap(task.getName()), task.getId());
        var pmReview = new PMReviewElement(task.getId(), name, name);

        props.forEach(property -> {
            var key = property.getCamundaName().toLowerCase();
            if (PM_REVIEW_TYPE.equals(key)) {
                pmReview.setReviewType(getEnum(PMReviewType.class, key, property.getCamundaValue()));
            }
            pmReview.getProperties().put(key, property.getCamundaValue());
        });
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MIN, DEFAULT_PM_REVIEW_MIN);
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MAX, DEFAULT_PM_REVIEW_MAX);

        String formKey;
        if (task instanceof UserTask) {
            var userTask = (UserTask) task;
            formKey = userTask.getCamundaFormKey();
        } else {
            formKey = pmReview.getProperties().get(PM_FORM_KEY);
        }
        if (!StringUtils.isBlank(formKey)) {
            try {
                var formName = getFormName(formKey);
                var formJson = resourceProvider.resourceToString(formName);

                pmReview.setForm(new PMFormElement(formKey, formName, formJson));
            } catch (Exception e) {
                throw parseException(PM_PARSE_NOT_FOUND, Map.of(KEY, "formKey", VALUE, formKey), formKey, e);
            }
        }
        return pmReview;
    }

    String getFormName(String key) {
        var matcher = FORM_NAME_PATTERN.matcher(key);
        return matcher.find() ? matcher.group(1) : null;
    }

    static String defaultValue(String checking, String defaultValue) {
        return StringUtils.isBlank(checking) ? defaultValue : checking.trim();
    }

    static String unwrap(String original) {
        if (!StringUtils.isBlank(original)) {
            return original.trim().replaceAll("[\\r|\\n]", " ").replaceAll(" +", " ");
        }
        return null;
    }

    private <E extends Enum<E>> E getEnum(Class<E> type, String name, String value) {
        if (StringUtils.isBlank(value)) {
            throw parseException(PM_PARSE_IS_BLANK, Map.of(KEY, name), name, null);
        }
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (Exception e) {
            throw parseException(PM_PARSE_NOT_FOUND, Map.of(KEY, name, VALUE, value), name, e);
        }
    }

    private ParseException parseException(ErrorCodeAware errorCode, Map<String, ?> params, String field, Throwable cause) {
        return new ParseException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), field, cause);
    }
}