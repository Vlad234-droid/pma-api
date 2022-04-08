package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BuildNextCycleHandler extends CamundaAbstractFlowHandler {

    private static final String ERROR = "build_error";
    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var event = context.getEvent();
        if (!validateEvent(event)) {
            throw new BpmnError(ERROR, "Event is null or does not contain properties to repeat cycle.");
        }
        var previousCycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        if (previousCycle.getCreatedBy() == null || previousCycle.getTemplate() == null) {
            throw new BpmnError(ERROR, "Previous cycle invalid. Template or created by is null.");
        }
        var eventProperties = event.getEventProperties();
        var left = (int) eventProperties.get(FlowParameters.PM_CYCLE_REPEATS_LEFT.name());
        if (!needToRepeat(previousCycle, left)) {
            throw new BpmnError(ERROR, "Don't need to repeat cycle. It seems the same event sends again.");
        }
        initPropertiesMap(previousCycle);
        previousCycle.getProperties().getMapJson().put(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), String.valueOf(left));
        previousCycle.setStartTime(previousCycle.getStartTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setEndTime(previousCycle.getEndTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setUuid(null);
        context.setVariable(FlowParameters.PM_CYCLE, previousCycle);
    }

    private void initPropertiesMap(PMCycle previousCycle) {
        if (previousCycle.getProperties() == null) {
            previousCycle.setProperties(new MapJson());
        }
        if (previousCycle.getProperties().getMapJson() == null) {
            previousCycle.getProperties().setMapJson(new HashMap<>());
        }
    }

    private boolean needToRepeat(PMCycle previousCycle, int left) {
        var existCyclesCount = getExistCyclesCount(previousCycle);
        var max = getPmCycleMax(previousCycle);
        return existCyclesCount <= max - left;
    }

    private int getExistCyclesCount(PMCycle previousCycle) {
        var requestQuery = new RequestQuery();
        var condition1 = new Condition("entry-config-key", Condition.Operand.EQUALS, previousCycle.getEntryConfigKey());
        var condition2 = new Condition("template-uuid", Condition.Operand.EQUALS, previousCycle.getTemplate().getUuid());
        requestQuery.setFilters(List.of(condition1, condition2));
        return pmCycleService.findAll(requestQuery, false).size();
    }

    private int getPmCycleMax(PMCycle previousCycle) {
        var max = 0;
        if (previousCycle.getMetadata() != null && previousCycle.getMetadata().getCycle() != null
                && previousCycle.getMetadata().getCycle().getProperties() != null) {
            max = Integer.parseInt(previousCycle.getMetadata().getCycle().getProperties().getOrDefault(PMCycleElement.PM_CYCLE_MAX, "0"));
        }
        return max;
    }

    private boolean validateEvent(Event event) {
        if (event == null) {
            return false;
        }
        var eventProperties = event.getEventProperties();
        return eventProperties.containsKey(FlowParameters.PM_CYCLE_UUID.name())
                && eventProperties.containsKey(FlowParameters.PM_CYCLE_REPEATS_LEFT.name());
    }
}
