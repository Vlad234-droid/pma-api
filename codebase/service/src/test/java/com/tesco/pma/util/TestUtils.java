package com.tesco.pma.util;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.ExternalSystems;
import com.tesco.pma.colleague.api.IamSourceSystem;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.Job;
import com.tesco.pma.colleague.api.workrelationships.LegalEmployer;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-12 23:55
 */
@UtilityClass
public class TestUtils {
    public enum KEYS {
        COLLEAGUE,
        COLLEAGUE_UUID,
        PM_CYCLE_KEY,

        COUNTRY_CODE,
        LEGAL_EMPLOYER_NAME,
        BUSINESS_TYPE,
        WORK_LEVEL,
        SALARY_FREQUENCY,
        JOB_NAME,
        IAM_SOURCE,

        GROUP_A_V1,
        GROUP_A_V2,
        GROUP_B,
        GROUP_C
    }

    public static Colleague createColleague(Map<KEYS, Object> params) {
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
        colleague.setColleagueUUID((UUID) params.get(KEYS.COLLEAGUE_UUID));
        colleague.setWorkRelationships(List.of(wr));
        if (params.containsKey(KEYS.COUNTRY_CODE)) {
            colleague.setCountryCode((String) params.get(KEYS.COUNTRY_CODE));
        }
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
