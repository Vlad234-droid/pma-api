package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMCycleRepeatHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        Event event = context.getEvent();
        if (!validateEvent(event)) {
            log.error("Event is null or does not contain properties to repeat cycle. Event: {}", event);
            return;
        }
        Map<String, Serializable> eventProperties = event.getEventProperties();
        UUID cycleUuid = UUID.fromString(eventProperties.get(FlowParameters.PM_CYCLE_UUID.name()).toString());
        PMCycle previousCycle = pmCycleService.get(cycleUuid, false).getCycle();
        if (previousCycle.getCreatedBy() == null || previousCycle.getTemplate() == null) {
            log.error("Previous cycle invalid. Template or created by is null. Cycle: {}", previousCycle);
            return;
        }
        int left = (int) eventProperties.get(FlowParameters.PM_CYCLE_REPEAT_COUNT.name());
        if (!needToRepeat(previousCycle, left)) {
            log.debug("Don't need to repeat cycle. It seems event duplicates {}", event);
            return;
        }
        initPropertiesMap(previousCycle);
        previousCycle.getProperties().getMapJson().put(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(), String.valueOf(left));
        previousCycle.setStartTime(previousCycle.getStartTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setEndTime(previousCycle.getEndTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setUuid(null);
        PMCycle nextCycle = pmCycleService.create(previousCycle, previousCycle.getCreatedBy().getUuid());
        PMCycle updatedCycle = pmCycleService.updateStatus(nextCycle.getUuid(), PMCycleStatus.REGISTERED);
        pmCycleService.start(updatedCycle);
        context.setVariable(FlowParameters.PM_CYCLE, updatedCycle);
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
        int existCyclesCount = getExistCyclesCount(previousCycle);
        int max = getPmCycleMax(previousCycle);
        return existCyclesCount <= max - left;
    }

    private int getExistCyclesCount(PMCycle previousCycle) {
        RequestQuery requestQuery = new RequestQuery();
        Condition condition1 = new Condition("entry-config-key", Condition.Operand.EQUALS, previousCycle.getEntryConfigKey());
        Condition condition2 = new Condition("template-uuid", Condition.Operand.EQUALS, previousCycle.getTemplate().getUuid());
        requestQuery.setFilters(List.of(condition1, condition2));
        return pmCycleService.getAll(requestQuery, false).size();
    }

    private int getPmCycleMax(PMCycle previousCycle) {
        int max = 0;
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
        Map<String, Serializable> eventProperties = event.getEventProperties();
        return eventProperties.containsKey(FlowParameters.PM_CYCLE_UUID.name())
                && eventProperties.containsKey(FlowParameters.PM_CYCLE_REPEAT_COUNT.name());
    }
}
