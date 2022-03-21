package com.tesco.pma.cycle.service;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmCycleMappingServiceImpl implements PmCycleMappingService {

    private static final String PM_CYCLE_MAPPING_TABLE_KEY = "pm_cycle_mapping";

    private final ProfileService profileService;
    private final ProcessEngine processEngine;

    @Override
    public String getPmCycleMappingKey(UUID colleagueUuid) {
        var colleague = profileService.findColleagueByColleagueUuid(colleagueUuid);
        var decisionService = processEngine.getDecisionService();
        var variables = Variables.createVariables().putValue(FlowParameters.COLLEAGUE.name(), colleague);

        var result =
                decisionService.evaluateDecisionTableByKey(PM_CYCLE_MAPPING_TABLE_KEY, variables);

        return result.getSingleEntry();
    }
}
