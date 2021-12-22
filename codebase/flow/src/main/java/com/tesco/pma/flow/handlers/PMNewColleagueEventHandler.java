package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMNewColleagueEventHandler extends AbstractColleagueCycleHandler {

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
                pmCycleService.getAll(true)
                        .stream()
                        .filter(pmCycle -> PMCycleStatus.ACTIVE == pmCycle.getStatus())
                        .filter(c -> configEntryService.isColleagueExistsForCompositeKey(colleagueUuid, c.getEntryConfigKey()))
                        .findFirst()
                        .ifPresentOrElse(cycle -> {
                                    pmColleagueCycleService.saveColleagueCycles(List.of(mapToColleagueCycle(colleagueUuid, cycle)));
                                    context.setVariable(FlowParameters.PM_CYCLE, cycle);
                                    context.setVariable(FlowParameters.START_DATE, defineStartDate(cycle));
                                },
                                () -> log.warn(LogFormatter.formatMessage(messageSourceAccessor, PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE,
                                        Map.of(COLLEAGUE_UUID_PARAMETER, colleagueUuid))));
            }
        }
    }

}
