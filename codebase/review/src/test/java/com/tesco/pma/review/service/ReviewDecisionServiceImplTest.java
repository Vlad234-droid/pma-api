package com.tesco.pma.review.service;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.review.service.TimelinePointDecisionServiceImplTest.getTimelinePointStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ReviewDecisionServiceImplTest {

    private static final String REVIEW_ALLOWED_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_statuses.dmn";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_prev_statuses.dmn";
    private static final String REVIEW_ALLOWED_STATUSES_TABLE_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID = "review_allowed_prev_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    private DmnEngine dmnEngine;

    @BeforeEach
    void init() {
        dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
    }

    @Test
    void reviewAllowedStatusesTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, MYR.getCode())
                .putValue(OPERATION_VAR_KEY, "CREATE");

        var statuses = getTimelinePointStatus(REVIEW_ALLOWED_STATUSES_PATH,
                REVIEW_ALLOWED_STATUSES_TABLE_ID,
                variables,
                dmnEngine);

        assertEquals(2, statuses.size());
    }

    @Test
    void reviewAllowedPrevStatusesTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, OBJECTIVE.getCode())
                .putValue(NEW_STATUS_VAR_KEY, WAITING_FOR_APPROVAL.getCode());

        var statuses = getTimelinePointStatus(REVIEW_ALLOWED_PREV_STATUSES_PATH,
                REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables,
                dmnEngine);

        assertEquals(4, statuses.size());
    }
}
