package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_COLLEAGUE_UUID_ABSENT;
import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_INVALID_COLLEAGUE_UUID_FORMAT;
import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_NOT_FOUND_FOR_COLLEAGUE;

@Component
@RequiredArgsConstructor
public class PMNewColleagueEventHandler extends AbstractColleagueCycleHandler {

    private static final String COLLEAGUE_UUID_PARAMETER = "colleagueUuid";
    private static final String STATUS_FIELD_FILTER = "status";

    private final ConfigEntryService configEntryService;
    private final PMColleagueCycleService pmColleagueCycleService;
    private final PMCycleService pmCycleService;
    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) {
        try {
            final var colleagueUuid = HandlerUtils.getEventColleagueUuid(context);

            if (colleagueUuid != null) {
                var requestQuery = new RequestQuery();
                requestQuery.addFilters(STATUS_FIELD_FILTER, ACTIVE.getId());

                pmCycleService.getAll(requestQuery, true)
                        .stream()
                        .filter(c -> configEntryService.isColleagueExistsForCompositeKey(colleagueUuid, c.getEntryConfigKey()))
                        .findFirst()
                        .ifPresentOrElse(cycle -> {
                                    ColleagueEntity colleague = profileService.getColleague(colleagueUuid);
                                    pmColleagueCycleService.create(mapToColleagueCycle(colleague, cycle));
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
