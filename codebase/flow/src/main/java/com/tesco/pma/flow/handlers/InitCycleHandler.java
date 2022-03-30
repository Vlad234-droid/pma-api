package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.tesco.pma.cycle.api.model.PMCycleElement.PM_CYCLE_BEFORE_END;
import static com.tesco.pma.cycle.api.model.PMCycleElement.PM_CYCLE_BEFORE_START;
import static com.tesco.pma.flow.exception.ErrorCodes.PARAMETER_CANNOT_BE_READ;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-07 22:37
 */
@Component
@RequiredArgsConstructor
public class InitCycleHandler extends CamundaAbstractFlowHandler {
    private static final String INIT_ERROR = "init_error";
    private static final String P_PARAMETER_NAME = "property";

    private final PMCycleService cycleService;
    private final NamedMessageSourceAccessor messages;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var cycle = findCycle(context);
        context.setVariable(FlowParameters.PM_CYCLE, cycle);

        var colleagueUuid = getOptionalVariableDeep(context, FlowParameters.COLLEAGUE_UUID, UUID.class, UUID::fromString);
        var scheduled = getOptionalVariableDeep(context, FlowParameters.SCHEDULED, Boolean.class, Boolean::valueOf);

        if (colleagueUuid == null && scheduled == null) {
            throw new BpmnError(INIT_ERROR, "Neither colleagueUuid nor scheduled are defined");
        }
        if (colleagueUuid != null) {
            context.setVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid);
        }
        var scheduleCycle = colleagueUuid == null && BooleanUtils.isTrue(scheduled);
        context.setVariable(FlowParameters.SCHEDULED, scheduleCycle);
        context.setVariable(FlowParameters.CYCLE_START_DATE, cycle.getStartTime());
        context.setVariable(FlowParameters.CYCLE_START_DATE_S,
                HandlerUtils.formatDate(HandlerUtils.instantToDate(cycle.getStartTime())));
        context.setVariable(FlowParameters.CYCLE_END_DATE, cycle.getEndTime());
        context.setVariable(FlowParameters.CYCLE_END_DATE_S,
                HandlerUtils.formatDate(HandlerUtils.instantToDate(cycle.getEndTime())));

        setBeforeDateVariables(context, cycle);
    }

    private void setBeforeDateVariables(ExecutionContext context, PMCycle cycle) {
        var beforeStart = context.getNullableVariable(PM_CYCLE_BEFORE_START, String.class);
        if (StringUtils.isNotEmpty(beforeStart)) {
            var period = Period.parse(beforeStart);
            var beforeStartDate = cycle.getStartTime().minus(period);
            context.setVariable(FlowParameters.CYCLE_BEFORE_START_DATE, beforeStartDate);
            context.setVariable(FlowParameters.CYCLE_BEFORE_START_DATE_S,
                    HandlerUtils.formatDate(HandlerUtils.instantToDate(beforeStartDate)));
        }
        var beforeEnd = context.getNullableVariable(PM_CYCLE_BEFORE_END, String.class);
        if (StringUtils.isNotEmpty(beforeEnd)) {
            var period = Period.parse(beforeEnd);
            var before = cycle.getEndTime().minus(period);
            var beforeEndDate = before.isBefore(cycle.getStartTime()) ? cycle.getStartTime() : before;
            context.setVariable(FlowParameters.CYCLE_BEFORE_END_DATE, beforeEndDate);
            context.setVariable(FlowParameters.CYCLE_BEFORE_END_DATE_S,
                    HandlerUtils.formatDate(HandlerUtils.instantToDate(beforeEndDate)));
        }
    }

    private PMCycle findCycle(ExecutionContext context) {
        var cycle = getOptionalVariable(context, FlowParameters.PM_CYCLE, PMCycle.class, null);
        if (cycle == null) {
            var cycleUuid = getOptionalVariableDeep(context, FlowParameters.PM_CYCLE_UUID, UUID.class, UUID::fromString);
            if (cycleUuid != null) {
                return findCycle(cycleUuid);
            }
            throw new BpmnError(INIT_ERROR, "Neither cycle nor cycle UUID is defined");
        }
        return cycle;
    }

    private PMCycle findCycle(UUID cycleUuid) {
        try {
            var cycleResponse = cycleService.get(cycleUuid, false);
            return cycleResponse.getCycle();
        } catch (Exception e) {
            throw new BpmnError(INIT_ERROR, "Cycle was not found", e);
        }
    }

    private <E extends Enum<E>, T> T getOptionalVariable(Event event, E name, Class<T> tclass,
                                                         Function<String, T> fromString) {
        T value = getVariableQuietly(event, name, tclass);
        if (value == null && fromString != null) {
            var svalue = getVariableQuietly(event, name, String.class);
            if (svalue != null) {
                try {
                    value = fromString.apply(svalue);
                } catch (Exception e) {
                    log.info(messages.getMessage(PARAMETER_CANNOT_BE_READ, Map.of(P_PARAMETER_NAME, FlowParameters.COLLEAGUE_UUID)));
                }
            }
        }
        return value;
    }

    private <E extends Enum<E>, T> T getOptionalVariable(ExecutionContext context, E name, Class<T> tclass,
                                                         Function<String, T> fromString) {
        T value = getVariableQuietly(context, name, tclass);
        if (value == null && fromString != null) {
            String svalue = getVariableQuietly(context, name, String.class);
            if (svalue != null) {
                try {
                    value = fromString.apply(svalue);
                } catch (Exception e) {
                    log.info(messages.getMessage(PARAMETER_CANNOT_BE_READ, Map.of(P_PARAMETER_NAME, FlowParameters.COLLEAGUE_UUID)));
                }
            }
        }
        return value;
    }

    private <E extends Enum<E>, T> T getOptionalVariableDeep(ExecutionContext context, E name, Class<T> tclass,
                                                             Function<String, T> fromString) {
        T value = getOptionalVariable(context, name, tclass, fromString);
        if (value == null && context.getEvent() != null) {
            return getOptionalVariable(context.getEvent(), name, tclass, fromString);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>, T> T getVariableQuietly(Event event, E name, Class<T> tclass) { //NOPMD
        try {
            return (T) event.getEventProperty(name.name());
        } catch (Exception e) {
            log.debug("Invalid type", e);
        }
        return null;
    }

    private <E extends Enum<E>, T> T getVariableQuietly(ExecutionContext context, E name, Class<T> tclass) {
        try {
            T value = context.getNullableVariable(name, tclass);
            return value != null && value.getClass().equals(tclass) ? value : null; // since generic does not work
        } catch (Exception e) {
            log.debug("Invalid type", e);
        }
        return null;
    }
}
