package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PMCycleMappingServiceImpl implements PMCycleMappingService {

    private static final String PM_CYCLE_MAPPING_TABLE_KEY = "pm_cycle_mapping";

    private final ProcessEngine processEngine;

    @Override
    public Set<String> getPmCycleMappingKeys() {
        return DmnTablesUtils.getAllResults(processEngine.getRepositoryService(), PM_CYCLE_MAPPING_TABLE_KEY);
    }
}
