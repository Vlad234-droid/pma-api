package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_COLLEAGUE_UUID_ABSENT;
import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_INVALID_COLLEAGUE_UUID_FORMAT;
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
        try {
            final UUID colleagueUuid = HandlerUtils.getEventColleagueUuid(context);
            var requestQuery = new RequestQuery();
            requestQuery.setFilters(Collections.emptyList());

            if (colleagueUuid != null) {
                pmCycleService.getAll(requestQuery, true)
                        .stream()
                        .filter(pmCycle -> PMCycleStatus.ACTIVE == pmCycle.getStatus())
                        .filter(c -> configEntryService.isColleagueExistsForCompositeKey(colleagueUuid, c.getEntryConfigKey()))
                        .findFirst()
                        .ifPresentOrElse(cycleOriginal -> {
                                    var cycle = adjustStartDate(cycleOriginal);
                                    pmColleagueCycleService.saveColleagueCycles(List.of(mapToColleagueCycle(colleagueUuid, cycle)));
                                    context.setVariable(FlowParameters.PM_CYCLE, cycle);
                                },
                                () -> log.warn(LogFormatter.formatMessage(messageSourceAccessor, PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE,
                                        Map.of(COLLEAGUE_UUID_PARAMETER, colleagueUuid))));
            } else {
                log.warn(LogFormatter.formatMessage(messageSourceAccessor, EVENT_COLLEAGUE_UUID_ABSENT));
            }
        } catch (IllegalArgumentException e) {
            log.warn(LogFormatter.formatMessage(messageSourceAccessor, EVENT_INVALID_COLLEAGUE_UUID_FORMAT));
        }
    }

}
