package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.flow.FlowParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static com.tesco.pma.cycle.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_BEFORE_END;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_BEFORE_START;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_DURATION;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_START;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_START_DELAY;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_DELAY;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_TIME;

/**
 * Calculates all required properties for timeline handlers
 **/

@Slf4j
@Component
public class InitPropertiesHandler extends CamundaAbstractFlowHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var parent = HandlerUtils.getParentModelElement(context);
        Stream.of(PM_TIMELINE_POINT_START_TIME, PM_TIMELINE_POINT_START_DELAY, PM_REVIEW_START,
                PM_REVIEW_START_DELAY, PM_REVIEW_BEFORE_START, PM_REVIEW_BEFORE_END, PM_REVIEW_DURATION, PM_TYPE)
                .forEach(prop -> copyFromParentIfNotExistsInContext(context, parent, prop));

        context.setVariable(FlowParameters.MODEL_PARENT_ELEMENT, parent);
    }

    private void copyFromParentIfNotExistsInContext(ExecutionContext context, PMElement parent, String property) {
        var nullableVariable = context.getNullableVariable(property);
        if (null == nullableVariable) {

            var parentProperties = parent.getProperties();
            if (parentProperties.containsKey(property)) {
                context.setVariable(property, parentProperties.get(property));
            }
        }
    }
}