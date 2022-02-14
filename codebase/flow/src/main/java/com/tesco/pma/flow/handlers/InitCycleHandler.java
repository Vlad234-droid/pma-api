package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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

        var colleagueUuid = getOptionalVariableDeep(context, FlowParameters.COLLEAGUE_UUID, String.class, null);
        var scheduled = getOptionalVariableDeep(context, FlowParameters.SCHEDULED, Boolean.class, Boolean::valueOf);

        if (colleagueUuid == null && scheduled == null) {
            throw new BpmnError(INIT_ERROR, "Neither colleagueUuid nor scheduled are defined");
        }
        if (colleagueUuid != null) {
            context.setVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid);
        }
        context.setVariable(FlowParameters.SCHEDULED, colleagueUuid == null && scheduled != null && scheduled);
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
