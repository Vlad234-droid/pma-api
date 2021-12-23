package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        var event = context.getEvent();
        if (event != null) {
            var eventProperties = event.getEventProperties();
            if (eventProperties.containsKey(EventParams.COLLEAGUE_UUID.name())) {
                var colleagueUuid = UUID.fromString(eventProperties.get(EventParams.COLLEAGUE_UUID.name()).toString());
                cycle = pmCycleService.getAll(true)
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
            List<ColleagueEntity> colleagues;
            var hireDate = PMCycleType.HIRING == cycle.getType() ? LocalDate.now() : null;
            colleagues = pmColleagueCycleService.findColleagues(cycle.getEntryConfigKey(), hireDate, true);
            ArrayList<PMColleagueCycle> colleagueCycles = new ArrayList<>();
            for (ColleagueEntity colleague : colleagues) {
                colleagueCycles.add(mapToColleagueCycle(colleague.getUuid(), cycle));
            }
            pmColleagueCycleService.saveColleagueCycles(colleagueCycles);
        }
        context.setVariable(FlowParameters.START_DATE, defineStartDate(cycle));
        context.setVariable(FlowParameters.PM_CYCLE, cycle);
    }

    private String defineStartDate(PMCycle cycle) {
        LocalDate startDate = LocalDate.ofInstant(cycle.getStartTime(), ZoneId.systemDefault());
        if (PMCycleType.HIRING == cycle.getType()) {
            startDate = LocalDate.now();
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(startDate);
    }

    private PMColleagueCycle mapToColleagueCycle(UUID colleagueUuid, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleagueUuid);
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        var startTime = cycle.getStartTime();
        var endTime = cycle.getEndTime();
        if (PMCycleType.HIRING == cycle.getType()) {
            startTime = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
            endTime = LocalDate.now().plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        cc.setStartTime(startTime);
        cc.setEndTime(endTime);
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
