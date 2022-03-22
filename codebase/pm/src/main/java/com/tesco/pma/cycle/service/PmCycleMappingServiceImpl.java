package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class PmCycleMappingServiceImpl implements PmCycleMappingService {

    private static final String PM_CYCLE_MAPPING_TABLE_PATH = "com/tesco/pma/flow/pm_cycle_mapping.dmn";
    private static final String PM_CYCLE_MAPPING_TABLE_KEY = "pm_cycle_mapping";

    private final ProcessEngine processEngine;

    @Override
    public Set<String> getPmCycleMappingKeys() {
        return DmnTablesUtils.getAllResults(processEngine.getRepositoryService(), PM_CYCLE_MAPPING_TABLE_KEY);
    }
}
