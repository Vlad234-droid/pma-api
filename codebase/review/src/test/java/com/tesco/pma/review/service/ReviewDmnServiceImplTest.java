package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.review.service.ReviewDmnServiceImpl.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReviewDmnServiceImplTest {

    private static final String REVIEW_ALLOWED_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_statuses.dmn";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_PATH = "com/tesco/pma/flow/review/review_allowed_prev_statuses.dmn";
    private static final String TL_POINT_ALLOWED_PREV_STATUSES_PATH = "com/tesco/pma/flow/review/tl_point_allowed_prev_statuses.dmn";
    private static final String TL_POINT_STATUS_TO_EVENT_PATH = "com/tesco/pma/flow/review/tl_point_status_to_event.dmn";
    private static final String REVIEW_ALLOWED_STATUSES_TABLE_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID = "review_allowed_prev_statuses_table";
    private static final String TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID = "tl_point_allowed_prev_statuses_table";
    private static final String TL_POINT_STATUS_TO_EVENT_TABLE_ID = "tl_point_status_to_event_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String STATUS_VAR_KEY = "STATUS";
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
                variables);

        assertEquals(2, statuses.size());
    }

    @Test
    void reviewAllowedPrevStatusesTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, OBJECTIVE.getCode())
                .putValue(NEW_STATUS_VAR_KEY, WAITING_FOR_APPROVAL.getCode());

        var statuses = getTimelinePointStatus(REVIEW_ALLOWED_PREV_STATUSES_PATH,
                REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables);

        assertEquals(4, statuses.size());
    }

    @Test
    void tlPointAllowedPrevStatusesTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(NEW_STATUS_VAR_KEY, APPROVED.getCode());

        var statuses = getTimelinePointStatus(TL_POINT_ALLOWED_PREV_STATUSES_PATH,
                TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables);

        assertEquals(6, statuses.size());
    }

    @Test
    void tlPointStatusToEventTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(STATUS_VAR_KEY, APPROVED.getCode());

        var result = getDmnDecisionRuleResults(TL_POINT_STATUS_TO_EVENT_PATH,
                TL_POINT_STATUS_TO_EVENT_TABLE_ID,
                variables);

        assertEquals("NF_PM_REVIEW_APPROVED", result.getSingleEntry());
    }

    private Collection<PMTimelinePointStatus> getTimelinePointStatus(String dmnPath, String dmnId, VariableMap variables) throws IOException {
        DmnDecisionTableResult result = getDmnDecisionRuleResults(dmnPath, dmnId, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }

    private DmnDecisionTableResult getDmnDecisionRuleResults(String dmnPath, String dmnId, VariableMap variables) throws IOException {
        DmnDecision decision;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dmnPath)) {
            decision = dmnEngine.parseDecision(dmnId, inputStream);
        }

        return dmnEngine.evaluateDecisionTable(decision, variables);
    }
}
