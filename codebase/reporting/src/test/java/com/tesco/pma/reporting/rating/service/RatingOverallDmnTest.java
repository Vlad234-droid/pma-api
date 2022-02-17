package com.tesco.pma.reporting.rating.service;

import com.tesco.pma.flow.FlowParameters;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class RatingOverallDmnTest {

    private static final String PATH = "com/tesco/pma/flow/reporting/rating/overall_rating.dmn";
    private static final String DMN_ID = "overall_rating_table";
    private static final String WHAT_RATING_VAR_KEY = "WHAT_RATING";
    private static final String HOW_RATING_VAR_KEY = "HOW_RATING";

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
    void outstandingVsSatisfactoryTest() {

        var variables = Variables.createVariables()
                .putValue(WHAT_RATING_VAR_KEY, "Outstanding")
                .putValue(HOW_RATING_VAR_KEY, "Satisfactory");

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        assertEquals("Satisfactory", result.getFirstResult().getEntry(FlowParameters.RESULT.name()));
    }

}
