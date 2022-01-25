package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMCycleRepeatHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        String repeatCount = context.getVariable(FlowParameters.PM_CYCLE_REPEAT_COUNT, String.class);
        if (StringUtils.isBlank(repeatCount)) {
            return;
        }
        var event = context.getEvent();
        if (event == null) {
            return;
        }
        var eventProperties = event.getEventProperties();
        if (!eventProperties.containsKey(EventParams.PM_CYCLE_UUID.name())) {
            return;
        }
        UUID cycleUuid = UUID.fromString(eventProperties.get(EventParams.PM_CYCLE_UUID.name()).toString());
        PMCycle previousCycle = pmCycleService.get(cycleUuid, false).getCycle();
        int previousCycleRepeatCount =
                Integer.parseInt(previousCycle.getProperties().getMapJson().getOrDefault(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(),
                        "0")) - 1;
        if (Integer.parseInt(repeatCount) == previousCycleRepeatCount) {
            return;
        }
        previousCycle.getProperties().getMapJson().put(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(), repeatCount);
        previousCycle.setStartTime(previousCycle.getStartTime().plus(Period.ofYears(1)));
        previousCycle.setEndTime(previousCycle.getEndTime().plus(Period.ofYears(1)));
        PMCycle nextCycle = pmCycleService.create(previousCycle, previousCycle.getCreatedBy().getUuid());
        pmCycleService.start(nextCycle.getUuid());
        context.setVariable(FlowParameters.PM_CYCLE, nextCycle);
    }
}
