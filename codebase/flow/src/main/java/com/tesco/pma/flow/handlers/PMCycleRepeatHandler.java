package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMCycleRepeatHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var event = context.getEvent();
        if (event == null) {
            return;
        }
        var eventProperties = event.getEventProperties();
        if (!eventProperties.containsKey(FlowParameters.PM_CYCLE_UUID.name())) {
            return;
        }
        if (!eventProperties.containsKey(FlowParameters.PM_CYCLE_REPEAT_COUNT.name())) {
            return;
        }
        UUID cycleUuid = UUID.fromString(eventProperties.get(FlowParameters.PM_CYCLE_UUID.name()).toString());
        PMCycle previousCycle = pmCycleService.get(cycleUuid, false).getCycle();
        if (previousCycle.getCreatedBy() == null || previousCycle.getTemplate() == null) {
            return;
        }
        int previousCycleRepeatCount = 0;
        if (previousCycle.getProperties() != null && previousCycle.getProperties().getMapJson() != null) {
            previousCycleRepeatCount =
                    Integer.parseInt(previousCycle.getProperties().getMapJson().getOrDefault(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(),
                            "0")) - 1;
        } else if (previousCycle.getMetadata() != null && previousCycle.getMetadata().getCycle() != null &&
                previousCycle.getMetadata().getCycle().getProperties() != null) {
            previousCycleRepeatCount =
                    Integer.parseInt(previousCycle.getMetadata().getCycle().getProperties().getOrDefault(PMCycleElement.PM_CYCLE_MAX,
                            "0"));
        }
        int repeatCount = Integer.parseInt(eventProperties.get(FlowParameters.PM_CYCLE_REPEAT_COUNT.name()).toString());
        if (repeatCount == previousCycleRepeatCount) {
            return;
        }
        if (previousCycle.getProperties() == null) {
            previousCycle.setProperties(new MapJson());
        }
        if (previousCycle.getProperties().getMapJson() == null) {
            previousCycle.getProperties().setMapJson(new HashMap<>());
        }
        previousCycle.getProperties().getMapJson().put(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(), String.valueOf(repeatCount));
        previousCycle.setStartTime(previousCycle.getStartTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setEndTime(previousCycle.getEndTime().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).toInstant());
        previousCycle.setUuid(null);
        PMCycle nextCycle = pmCycleService.publish(previousCycle, previousCycle.getCreatedBy().getUuid());
        context.setVariable(FlowParameters.PM_CYCLE, nextCycle);
    }
}
