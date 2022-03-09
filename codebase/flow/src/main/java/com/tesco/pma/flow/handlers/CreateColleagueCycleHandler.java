package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.exception.ErrorCodes;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateColleagueCycleHandler extends AbstractColleagueCycleHandler {
    private static final String P_STATUS = "status";
    private static final String P_CYCLE_UUID = "cycleUuid";
    private static final String P_COLLEAGUE_UUID = "colleagueUuid";

    private final PMColleagueCycleService pmColleagueCycleService;
    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messages;

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        var colleagueUuid = context.getVariable(FlowParameters.COLLEAGUE_UUID, UUID.class);

        PMColleagueCycle colleagueCycle;
        var colleagueCycles = pmColleagueCycleService.getByCycleUuid(cycle.getUuid(), colleagueUuid, cycle.getStatus());

        if (colleagueCycles.isEmpty()) {
            var colleague = profileService.getColleague(colleagueUuid);
            try {
                colleagueCycle = pmColleagueCycleService.create(mapToColleagueCycle(colleague, cycle));
            } catch (AlreadyExistsException e) {
                throw buildBpmnError(cycle, colleagueUuid); //NOPMD already logged
            }
        } else if (1 == colleagueCycles.size()) {
            colleagueCycle = colleagueCycles.get(0);
        } else {
            throw buildBpmnError(cycle, colleagueUuid);
        }
        context.setVariable(FlowParameters.PM_COLLEAGUE_CYCLE, colleagueCycle);
    }

    private BpmnError buildBpmnError(PMCycle cycle, UUID colleagueUuid) {
        return new BpmnError(ErrorCodes.PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS.getCode(),
                messages.getMessage(ErrorCodes.PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS,
                        Map.of(P_STATUS, cycle.getStatus(),
                                P_CYCLE_UUID, cycle.getUuid(),
                                P_COLLEAGUE_UUID, colleagueUuid)));
    }
}
