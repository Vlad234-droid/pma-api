
package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElement;
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
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Calculates all required timeline point's dates for a fiscal year performance cycle
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:25
 */
@Slf4j
@Component
public class ProcessTimelinePointHandler extends CamundaAbstractFlowHandler {

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

    private void createTimelinePoints(ExecutionContext context, PMCycle cycle) {
        var parent = getParent(context);
        var startDate = context.getVariable(FlowParameters.START_DATE, LocalDate.class);
        var startTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        List<PMColleagueCycle> colleagueCycles;
        if (PMCycleType.HIRING == cycle.getType()) {
            colleagueCycles = colleagueCycleService.getActiveByCycleUuidWithoutTimelinePoint(cycle.getUuid(), startTime);
        } else {
            colleagueCycles = colleagueCycleService.getActiveByCycleUuidWithoutTimelinePoint(cycle.getUuid(), null);
        }
        if (CollectionUtils.isEmpty(colleagueCycles)) {
            return;
        }

        var props = buildProps(context, startDate, parent);

        var endTime = Optional.ofNullable(context.getNullableVariable(FlowParameters.END_DATE, LocalDate.class))
                .map(date -> date.atStartOfDay().toInstant(ZoneOffset.UTC))
                .orElse(null);
        var timelinePoints = colleagueCycles.stream()
                .map(cc -> TimelinePoint.builder()
                        .uuid(UUID.randomUUID())
                        .colleagueCycleUuid(cc.getUuid())
                        .code(parent.getCode())
                        .description(parent.getDescription())
                        .type(parent.getType())
                        .startTime(startTime)
                        .endTime(endTime)
                        .properties(props)
                        .status(calculateStatus(startDate))
                        .build())
                .collect(Collectors.toList());

        timelinePointService.saveAll(timelinePoints);
    }

    PMElement getParent(ExecutionContext context) {
        return HandlerUtils.getParent(context);
    }

    private PMTimelinePointStatus calculateStatus(LocalDate startDate) {
        return startDate.isAfter(LocalDate.now()) ? PMTimelinePointStatus.NOT_STARTED : PMTimelinePointStatus.STARTED;
    }

    private MapJson buildProps(ExecutionContext context, LocalDate startDate, PMElement element) {
        var map = new LinkedHashMap<String, String>();
        map.put(FlowParameters.START_DATE.name(), HandlerUtils.formatDate(startDate));

        List.of(FlowParameters.END_DATE, FlowParameters.BEFORE_START_DATE, FlowParameters.BEFORE_END_DATE)
                .forEach(e -> Optional.ofNullable(context.getNullableVariable(e, LocalDate.class))
                        .ifPresent(v -> map.put(e.name(), HandlerUtils.formatDate(v))));

        List.of(PMReviewElement.PM_REVIEW_MIN, PMReviewElement.PM_REVIEW_MAX)
                .forEach(key -> Optional.ofNullable(element.getProperties().get(key))
                        .ifPresent(v -> map.put(key, v)));
        return new MapJson(map);
    }
}
