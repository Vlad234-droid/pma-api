package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.TimelinePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.cycle.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_CODE;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_DESCRIPTION;

/**
 * Calculates all required timeline point's dates for a fiscal year performance cycle
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:25
 */
@Slf4j
@Component
public class ProcessTimelinePointHandler extends AbstractUpdateEnumStatusHandler<PMCycleStatus> {

    @Autowired
    private PMColleagueCycleService colleagueCycleService;

    @Autowired
    private TimelinePointService timelinePointService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        if (PMCycleType.FISCAL != cycle.getType() && PMCycleType.HIRING != cycle.getType()) {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect cycle type: " + cycle.getType());
        }
        createTimelinePoints(context, cycle);
    }

    @Override
    protected Class<PMCycleStatus> getEnumClass() {
        return PMCycleStatus.class;
    }

    private void createTimelinePoints(ExecutionContext context, PMCycle cycle) {
        var startDate = context.getVariable(FlowParameters.START_DATE, LocalDate.class);
        var startTime = HandlerUtils.dateToInstant(startDate);
        var colleagueCycles = colleagueCycleService.getByCycleUuidWithoutTimelinePoint(cycle.getUuid(),
                PMCycleType.HIRING == cycle.getType() ? startTime : null, DictionaryFilter.includeFilter(getOldStatuses()));
        if (CollectionUtils.isEmpty(colleagueCycles)) {
            return;
        }
        var endTime = HandlerUtils.dateToInstant(context.getVariable(FlowParameters.END_DATE, LocalDate.class));
        var timelinePoints = colleagueCycles.stream()
                .map(cc -> TimelinePoint.builder()
                        .uuid(UUID.randomUUID())
                        .colleagueCycleUuid(cc.getUuid())
                        .code(context.getVariable(PM_TIMELINE_POINT_CODE))
                        .description(context.getVariable(PM_TIMELINE_POINT_DESCRIPTION))
                        .type(PMElementType.getByCode(context.getVariable(PM_TYPE)))
                        .startTime(startTime)
                        .endTime(endTime)
                        .properties(buildProps(context))
                        .status(calculateStatus(startDate))
                        .build())
                .collect(Collectors.toList());

        timelinePointService.saveAll(timelinePoints);
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

        List.of(PMReviewElement.PM_REVIEW_MIN, PMReviewElement.PM_REVIEW_MAX)
                .forEach(key -> Optional.ofNullable(context.getNullableVariable(key))
                        .map(Object::toString)
                        .ifPresent(v -> map.put(key, v)));
        return new MapJson(map);
    }
}
