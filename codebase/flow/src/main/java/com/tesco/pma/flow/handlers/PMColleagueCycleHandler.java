package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMColleagueCycleHandler extends CamundaAbstractFlowHandler {

    private static final String COLLEAGUE_UUID_PARAMETER = "colleagueUuid";

    private final ConfigEntryService configEntryService;
    private final PMColleagueCycleService pmColleagueCycleService;
    private final PMCycleService pmCycleService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) {
        var event = context.getEvent();
        if (event != null) {
            var eventProperties = event.getEventProperties();
            if (eventProperties.containsKey(EventParams.COLLEAGUE_UUID.name())) {
                var colleagueUuid = UUID.fromString(eventProperties.get(EventParams.COLLEAGUE_UUID.name()).toString());
                var cycle = pmCycleService.getAll(true)
                        .stream()
                        .filter(pmCycle -> PMCycleStatus.ACTIVE == pmCycle.getStatus())
                        .filter(c -> configEntryService.isColleagueExistsForCompositeKey(colleagueUuid, c.getEntryConfigKey()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE.getCode(),
                                messageSourceAccessor.getMessage(PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE,
                                        Map.of(COLLEAGUE_UUID_PARAMETER, colleagueUuid))));

                pmColleagueCycleService.saveColleagueCycles(Collections.singletonList(mapToColleagueCycle(colleagueUuid, cycle)));
                context.setVariable(FlowParameters.PM_CYCLE, cycle);

            }
        } else {
            var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
            var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());
            var colleagueCycles = colleagues.stream()
                    .map(c -> mapToColleagueCycle(c.getUuid(), cycle))
                    .collect(Collectors.toList());

            pmColleagueCycleService.saveColleagueCycles(colleagueCycles);
        }

    }

    private PMColleagueCycle mapToColleagueCycle(UUID colleagueUuid, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleagueUuid);
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        cc.setStartTime(cycle.getStartTime());
        cc.setEndTime(cycle.getEndTime());
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
