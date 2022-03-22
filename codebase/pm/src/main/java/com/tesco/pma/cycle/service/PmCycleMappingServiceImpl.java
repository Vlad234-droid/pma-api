package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class PmCycleMappingServiceImpl implements PmCycleMappingService {

    private static final String PM_CYCLE_MAPPING_TABLE_PATH = "com/tesco/pma/flow/pm_cycle_mapping.dmn";
    private static final String PM_CYCLE_MAPPING_TABLE_KEY = "pm_cycle_mapping";

    @Override
    public Set<String> getPmCycleMappingKeys() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(PM_CYCLE_MAPPING_TABLE_PATH)){
            return DmnTablesUtils.getAllResults(is, PM_CYCLE_MAPPING_TABLE_KEY);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptySet();
        }
    }
}
