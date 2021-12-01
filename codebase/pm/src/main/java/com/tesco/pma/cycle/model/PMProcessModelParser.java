package com.tesco.pma.cycle.model;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMElementType;
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
import java.util.stream.Collectors;

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
     * Utility method to get external properties
     *
     * @param element source element
     * @return collection of Camunda properties
     */
    public static Collection<CamundaProperty> getCamundaProperties(BaseElement element) {
        var extensionElements = element.getExtensionElements();
        if (extensionElements != null) {
            var propsQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
            if (propsQuery.count() > 0) {
                return propsQuery.singleResult().getCamundaProperties();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Utility method to get external properties
     *
     * @param element source element
     * @return map of properties
     */
    public static Map<String, String> getExternalProperties(BaseElement element) {
        return getExternalProperties(getCamundaProperties(element));
    }

    /**
     * Utility method to get external properties
     *
     * @param properties collection of Camunda properties
     * @return map of properties
     */
    public static Map<String, String> getExternalProperties(Collection<CamundaProperty> properties) {
        return properties.stream()
                .collect(Collectors.toMap(camundaProperty -> camundaProperty.getCamundaName().toLowerCase(),
                        CamundaProperty::getCamundaValue));
    }

    public static <T extends PMElement> T fillPMElement(Activity activity, T target) {
        return fillPMElement(activity, null, target);
    }

    public static <T extends PMElement> T fillPMElement(Activity activity, Collection<CamundaProperty> props, T target) {
        var name = defaultValue(unwrap(activity.getName()), activity.getId());
        target.setId(activity.getId());
        target.setCode(name);
        target.setDescription(name);
        target.setProperties(props == null ? getExternalProperties(activity) : getExternalProperties(props));
        if (target.getType() == null) {
            target.setType(PMElementType.getByCode(target.getProperties().get(PMElement.PM_TYPE)));
        }
        return target;
    }

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
        cycle.setCode(process.getName());
        var props = getExternalProperties(process);
        cycle.setProperties(props);
        cycle.setCycleType(getEnum(PMCycleType.class, PM_CYCLE_TYPE, props.get(PM_CYCLE_TYPE)));
        return cycle;
    }

    private void processTask(PMCycleElement cycle, Activity activity) {
        var props = getCamundaProperties(activity);
        if (props.isEmpty()) {
            return;
        }
        Optional<CamundaProperty> typeOptional = props.stream().filter(p ->
                p.getCamundaName().equalsIgnoreCase(PM_TYPE) && !StringUtils.isBlank(p.getCamundaValue())
        ).findFirst();
        if (typeOptional.isPresent()) {
            var type = typeOptional.get().getCamundaValue().toLowerCase();
            if (PM_REVIEW.equals(type)) {
                var review = parseReview(activity, props);
                cycle.getTimelinePoints().add(review);
            } else if (PM_TIMELINE_POINT.equals(type)) {
                cycle.getTimelinePoints().add(fillPMElement(activity, props, new PMTimelinePointElement()));
            }
        }
    }

    private PMReviewElement parseReview(Activity activity, Collection<CamundaProperty> props) {
        var pmReview = fillPMElement(activity, props, new PMReviewElement());
        pmReview.setReviewType(getEnum(PMReviewType.class, PM_REVIEW_TYPE, pmReview.getProperties().get(PM_REVIEW_TYPE)));
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MIN, DEFAULT_PM_REVIEW_MIN);
        pmReview.getProperties().putIfAbsent(PM_REVIEW_MAX, DEFAULT_PM_REVIEW_MAX);

        String formKey;
        if (activity instanceof UserTask) {
            var userTask = (UserTask) activity;
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