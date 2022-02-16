package com.tesco.pma.review.service;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReviewDmnServiceImplTest {

    private static final String PATH = "com/tesco/pma/flow/review/review_allowed_statuses.dmn";
    private static final String DMN_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";

    private DmnEngine dmnEngine;
    private DmnDecision decision;

    @BeforeEach
    void init() throws IOException {
        dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PATH)) {
            decision = dmnEngine.parseDecision(DMN_ID, inputStream);
        }
    }

    @Test
    void reviewAllowedStatusesTest() {

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, "MYR")
                .putValue(OPERATION_VAR_KEY, "CREATE");

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        var objects = result.collectEntries("STATUS");

        assertEquals(2,objects.size());
    }

}
