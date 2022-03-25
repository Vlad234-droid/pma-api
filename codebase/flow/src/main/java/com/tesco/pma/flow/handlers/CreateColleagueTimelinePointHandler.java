package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.TimelinePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.cycle.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_CODE;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_DESCRIPTION;

/**
 * Creates a colleague's timeline point
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-07 17:24
 */
@Component
@RequiredArgsConstructor
public class CreateColleagueTimelinePointHandler extends CamundaAbstractFlowHandler {

    private final TimelinePointService timelinePointService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var cycle = context.getVariable(FlowParameters.PM_COLLEAGUE_CYCLE, PMColleagueCycle.class);
        var timelinePoints = timelinePointService.get(cycle.getUuid(), context.getVariable(PM_TIMELINE_POINT_CODE), null);

        if (timelinePoints.isEmpty()) {
            createTimelinePoint(context, cycle);
        }
    }

    private void createTimelinePoint(ExecutionContext context, PMColleagueCycle colleagueCycle) {
        var startDate = context.getVariable(FlowParameters.START_DATE, LocalDate.class);
        var startTime = HandlerUtils.dateToInstant(startDate);
        var endTime = HandlerUtils.dateToInstant(context.getVariable(FlowParameters.END_DATE, LocalDate.class));
        var timelinePoint = TimelinePoint.builder()
                        .uuid(UUID.randomUUID())
                        .colleagueCycleUuid(colleagueCycle.getUuid())
                        .code(context.getVariable(PM_TIMELINE_POINT_CODE))
                        .description(context.getVariable(PM_TIMELINE_POINT_DESCRIPTION))
                        .type(PMElementType.getByCode(context.getVariable(PM_TYPE)))
                        .startTime(startTime)
                        .endTime(endTime)
                        .properties(buildProps(context))
                        .status(calculateStatus(startDate))
                        .build();
        context.setVariable(FlowParameters.TIMELINE_POINT, timelinePoint);
        try {
            timelinePointService.create(timelinePoint);
        } catch (AlreadyExistsException e) {
            log.debug("Timeline point already exists - nothing to do", e);
        }
    }

    private PMTimelinePointStatus calculateStatus(LocalDate startDate) {
        return startDate.isAfter(LocalDate.now()) ? PMTimelinePointStatus.NOT_STARTED : PMTimelinePointStatus.STARTED;
    }

    private MapJson buildProps(ExecutionContext context) {
        var map = new LinkedHashMap<String, String>();

        List.of(FlowParameters.START_DATE, FlowParameters.END_DATE, FlowParameters.BEFORE_START_DATE, FlowParameters.BEFORE_END_DATE)
                .forEach(e -> Optional.ofNullable(context.getNullableVariable(e))
                        .ifPresent(v -> {
                            var strValue = context.getNullableVariable(FlowParameters.getCorrespondedStringParameter(e), String.class);
                            if (strValue != null) {
                                map.put(e.name(), strValue);
                            }
                        }));
        var min = getVariable(context, PMReviewElement.PM_REVIEW_MIN, 1);
        var max = getVariable(context, PMReviewElement.PM_REVIEW_MAX, min);
        map.put(PMReviewElement.PM_REVIEW_MIN, min.toString());
        map.put(PMReviewElement.PM_REVIEW_MAX, max.toString());
        return new MapJson(map);
    }

    private Integer getVariable(ExecutionContext context, String name, int defaultValue) {
        String value = context.getNullableVariable(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                log.trace("The variable {} is not a number: {}", name, value, e);
            }
        }
        return defaultValue;
    }
}
