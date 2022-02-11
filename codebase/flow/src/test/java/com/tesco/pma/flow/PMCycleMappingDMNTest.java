package com.tesco.pma.flow;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.Job;
import com.tesco.pma.colleague.api.workrelationships.LegalEmployer;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private static final String NULL_VALUE = "__null__";
    private static final String OFFICE = "Office";

    private enum KEYS {
        COLLEAGUE,
        PM_CYCLE_KEY,

        LEGAL_EMPLOYER_NAME,
        BUSINESS_TYPE,
        WORK_LEVEL,
        SALARY_FREQUENCY,
        JOB_NAME,
        IAM_SOURCE,

        GROUP_A_V2,
        GROUP_B_V2,
        GROUP_C_V2
    }

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
        ), KEYS.GROUP_A_V2);
        assertSuccessRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, "Tesco",
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL5
        ), KEYS.GROUP_A_V2);
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
        ), KEYS.GROUP_B_V2);
    }

    @Test
    void groupBExcept() {
        assertExceptRule(Map.of(
                KEYS.LEGAL_EMPLOYER_NAME, TESCO_STORES_LIMITED,
                KEYS.BUSINESS_TYPE, OFFICE,
                KEYS.WORK_LEVEL, WorkLevel.WL1
        ));
    }

    private void assertSuccessRule(Map<KEYS, Object> params, KEYS expected) {
        var result = dmnEngine.evaluateDecisionTable(decision, buildVariables(Map.of(KEYS.COLLEAGUE, createColleague(params))));

        assertEquals(expected.name().toLowerCase(), result.getFirstResult().getEntry(KEYS.PM_CYCLE_KEY.name()));
    }

    private void assertExceptRule(Map<KEYS, Object> params) {
        var result = dmnEngine.evaluateDecisionTable(decision, buildVariables(Map.of(KEYS.COLLEAGUE, createColleague(params))));

        Assertions.assertTrue(result.isEmpty());
    }

    private VariableMap buildVariables(Map<KEYS, Object> params) {
        var variables = new VariableMapImpl();
        params.forEach((keys, v) -> variables.putValue(keys.name(), (v.equals(NULL_VALUE) ? null : v))); // NOPMD
        return variables;
    }

    private Colleague createColleague(Map<KEYS, Object> params) {
        var wr = new WorkRelationship();
        if (params.containsKey(KEYS.WORK_LEVEL)) {
            wr.setWorkLevel((WorkLevel) params.get(KEYS.WORK_LEVEL));
        }
        if (params.containsKey(KEYS.SALARY_FREQUENCY)) {
            wr.setSalaryFrequency((String) params.get(KEYS.SALARY_FREQUENCY));
        }
        if (params.containsKey(KEYS.LEGAL_EMPLOYER_NAME)) {
            var legalEmployer = new LegalEmployer();
            legalEmployer.setName((String) params.get(KEYS.LEGAL_EMPLOYER_NAME));
            wr.setLegalEmployer(legalEmployer);
        }
        if (params.containsKey(KEYS.BUSINESS_TYPE)) {
            var department = new Department();
            department.setBusinessType((String) params.get(KEYS.BUSINESS_TYPE));
            wr.setDepartment(department);
        }
        if (params.containsKey(KEYS.JOB_NAME)) {
            var job = new Job();
            job.setName((String) params.get(KEYS.JOB_NAME));
            wr.setJob(job);
        }
        var colleague = new Colleague();
        colleague.setColleagueUUID(UUID.fromString(COLLEAGUE_UUID));
        colleague.setWorkRelationships(List.of(wr));
        if (params.containsKey(KEYS.IAM_SOURCE)) {
            var externalSystems = new ExternalSystems();
            var iamSourceSystem = new IamSourceSystem();
            iamSourceSystem.setSource((String) params.get(KEYS.IAM_SOURCE));
            externalSystems.setIam(iamSourceSystem);
            colleague.setExternalSystems(externalSystems);
        }
        return colleague;
    }
}
