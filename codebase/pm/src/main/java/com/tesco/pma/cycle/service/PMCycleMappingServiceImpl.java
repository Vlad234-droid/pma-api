package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;
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
        return colleaguesUuids.stream()
                .map(profileService::findColleagueByColleagueUuid)
                .filter(Objects::nonNull)
                .filter(c -> !CollectionUtils.isEmpty(c.getWorkRelationships()))
                .collect(Collectors.toMap(Colleague::getColleagueUUID, this::getPmCycleMappingKey));
    }

    private String getPmCycleMappingKey(Colleague colleague) {

        var decisionService = processEngine.getDecisionService();
        var variables = Variables.createVariables().putValue(FlowParameters.COLLEAGUE.name(), colleague);

        var result =
                decisionService.evaluateDecisionTableByKey(PM_CYCLE_MAPPING_TABLE_KEY, variables);

        return StringUtils.defaultString(result.getSingleEntry());
    }
}
