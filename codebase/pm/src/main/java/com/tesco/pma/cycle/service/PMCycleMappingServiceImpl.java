package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PMCycleMappingServiceImpl implements PMCycleMappingService {

    private static final String PM_CYCLE_MAPPING_TABLE_KEY = "pm_cycle_mapping";

    private final ProfileService profileService;
    private final ProcessEngine processEngine;

    @Override
    public Set<String> getPmCycleMappingKeys() {
        return DmnTablesUtils.getAllResults(processEngine.getRepositoryService(), PM_CYCLE_MAPPING_TABLE_KEY);
    }

    @Override
    public Map<UUID, String> getPmCycleMappingKeys(List<UUID> colleaguesUuids) {
        return colleaguesUuids.stream().collect(Collectors.toMap(uuid -> uuid, this::getPmCycleMappingKey));
    }

    private String getPmCycleMappingKey(UUID colleagueUuid) {
        var colleague = profileService.findColleagueByColleagueUuid(colleagueUuid);

        if (colleague == null) {
            log.info("Colleague {} not found", colleagueUuid);
            return "";
        }

        if (CollectionUtils.isEmpty(colleague.getWorkRelationships())) {
            log.info("Colleague {} has no workRelationship", colleagueUuid);
            return "";
        }

        var decisionService = processEngine.getDecisionService();
        var variables = Variables.createVariables().putValue(FlowParameters.COLLEAGUE.name(), colleague);

        var result =
                decisionService.evaluateDecisionTableByKey(PM_CYCLE_MAPPING_TABLE_KEY, variables);

        var res = (String) result.getSingleEntry();
        return res == null ? "" : res;
    }
}
