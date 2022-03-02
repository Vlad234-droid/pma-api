package com.tesco.pma.flow;

import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.util.TestUtils.KEYS;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.util.TestUtils.createColleague;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-09 21:40
 */
class PMCycleMappingDMNTest {
    private static final String TESCO_STORES_LIMITED = "tesco stores limited";
    private static final String PATH = "com/tesco/pma/flow/pm_cycle_mapping.dmn";
    private static final String DMN_ID = "pm_cycle_mapping";
    private static final String COLLEAGUE_UUID = "efdbd43a-2436-4173-b4e2-6782c99a95ad";
    private static final String NULL_VALUE = "_null";
    private static final String OFFICE = "Office";

    private DmnEngine dmnEngine;
    private DmnDecision decision;

    @BeforeEach
    void init() throws IOException {
        if (decision == null) {
            dmnEngine = DmnEngineConfiguration
                    .createDefaultDmnEngineConfiguration()
                    .buildEngine();
            try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PATH)) {
                decision = dmnEngine.parseDecision(DMN_ID, inputStream);
            }
        }
    }

    @Test
    void groupAWl45() {
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "Tesco",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL4
        ), KEYS.GROUP_A_V1);

        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "Tesco",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL5
        ), KEYS.GROUP_A_V1);
    }

    @Test
    void groupAWl45Except() {
        assertExceptRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "tesco underwriting limited",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL5
        ));
    }

    @Test
    void groupAWl3() {
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "Tesco",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL3
        ), KEYS.GROUP_A_V2);
    }

    @Test
    void groupAWl3Except() {
        assertExceptRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "tesco underwriting limited",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL3,
                KEYS.IAM_SOURCE, "Tesco Mobile"
        ));
    }

    @Test
    void groupAWl2Annual() {
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, TESCO_STORES_LIMITED,
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL2,
                KEYS.SALARY_FREQUENCY, "Annual"
        ), KEYS.GROUP_A_V2);
    }

    @Test
    void groupBOffice() {
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, TESCO_STORES_LIMITED,
                KEYS.BUSINESS_TYPE, OFFICE
        ), KEYS.GROUP_B);
    }

    @Test
    void groupBExcept() {
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, TESCO_STORES_LIMITED,
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL1
        ), KEYS.GROUP_B);
    }

    private void assertSuccessRule(Map<KEYS, Object> iparams, KEYS expected) {
        Map<KEYS, Object> params = new HashMap<>(iparams);
        params.put(KEYS.COLLEAGUE_UUID, UUID.fromString(COLLEAGUE_UUID));
        var result = dmnEngine.evaluateDecisionTable(decision, buildVariables(Map.of(KEYS.COLLEAGUE, createColleague(params))));

        assertEquals(expected.name().toLowerCase(), result.getFirstResult().getEntry(KEYS.PM_CYCLE_KEY.name()));
    }

    private void assertExceptRule(Map<KEYS, Object> iparams) {
        Map<KEYS, Object> params = new HashMap<>(iparams);
        params.put(KEYS.COLLEAGUE_UUID, UUID.fromString(COLLEAGUE_UUID));
        var result = dmnEngine.evaluateDecisionTable(decision, buildVariables(Map.of(KEYS.COLLEAGUE, createColleague(params))));

        Assertions.assertTrue(result.isEmpty());
    }

    private VariableMap buildVariables(Map<KEYS, Object> params) {
        var variables = new VariableMapImpl();
        params.forEach((keys, v) -> variables.putValue(keys.name(), (v.equals(NULL_VALUE) ? null : v))); // NOPMD
        return variables;
    }
}
