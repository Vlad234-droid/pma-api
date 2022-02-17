package com.tesco.pma.review.service;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReviewDmnServiceImplTest {

    private static final String REVIEW_ALLOWED_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_statuses.dmn";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_prev_statuses.dmn";
    private static final String REVIEW_ALLOWED_STATUSES_DMN_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_DMN_ID = "review_allowed_prev_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String STATUS_VAR_KEY = "STATUS";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    private DmnEngine dmnEngine;

    @BeforeEach
    void init() throws IOException {
        dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
    }

    @Test
    void reviewAllowedStatusesTest() throws IOException {

        DmnDecision decision;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(REVIEW_ALLOWED_STATUSES_PATH)) {
            decision = dmnEngine.parseDecision(REVIEW_ALLOWED_STATUSES_DMN_ID, inputStream);
        }

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, MYR.getCode())
                .putValue(OPERATION_VAR_KEY, "CREATE");

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        var objects = result.collectEntries(STATUS_VAR_KEY);

        assertEquals(2, objects.size());
    }

    @Test
    void reviewAllowedPrevStatusesTest() throws IOException {

        DmnDecision decision;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(REVIEW_ALLOWED_PREV_STATUSES_PATH)) {
            decision = dmnEngine.parseDecision(REVIEW_ALLOWED_PREV_STATUSES_DMN_ID, inputStream);
        }

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, OBJECTIVE.getCode())
                .putValue(NEW_STATUS_VAR_KEY, WAITING_FOR_APPROVAL.getCode());

        var result = dmnEngine.evaluateDecisionTable(decision, variables);

        var objects = result.collectEntries(STATUS_VAR_KEY);

        assertEquals(4, objects.size());
    }

}
