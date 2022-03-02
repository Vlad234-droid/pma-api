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

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.review.service.TimelinePointDecisionServiceImpl.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TimelinePointDecisionServiceImplTest {

    private static final String TL_POINT_ALLOWED_PREV_STATUSES_PATH = "com/tesco/pma/flow/review/tl_point_allowed_prev_statuses.dmn";
    private static final String TL_POINT_STATUS_TO_EVENT_PATH = "com/tesco/pma/flow/review/tl_point_status_to_event.dmn";
    private static final String TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID = "tl_point_allowed_prev_statuses_table";
    private static final String TL_POINT_STATUS_TO_EVENT_TABLE_ID = "tl_point_status_to_event_table";
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
    void tlPointAllowedPrevStatusesTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(NEW_STATUS_VAR_KEY, APPROVED.getCode());

        var statuses = getTimelinePointStatus(TL_POINT_ALLOWED_PREV_STATUSES_PATH,
                TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables,
                dmnEngine);

        assertEquals(6, statuses.size());
    }

    @Test
    void tlPointStatusToEventTest() throws IOException {

        var variables = Variables.createVariables()
                .putValue(STATUS_VAR_KEY, APPROVED.getCode());

        var result = getDmnDecisionRuleResults(TL_POINT_STATUS_TO_EVENT_PATH,
                TL_POINT_STATUS_TO_EVENT_TABLE_ID,
                variables,
                dmnEngine);

        assertEquals("NF_PM_REVIEW_APPROVED", result.getSingleEntry());
    }

    static Collection<PMTimelinePointStatus> getTimelinePointStatus(String dmnPath, String dmnId, VariableMap variables, DmnEngine dmnEngine) throws IOException {
        DmnDecisionTableResult result = getDmnDecisionRuleResults(dmnPath, dmnId, variables, dmnEngine);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }

    static DmnDecisionTableResult getDmnDecisionRuleResults(String dmnPath, String dmnId, VariableMap variables, DmnEngine dmnEngine) throws IOException {
        DmnDecision decision;
        try (InputStream inputStream = TimelinePointDecisionServiceImplTest.class.getClassLoader().getResourceAsStream(dmnPath)) {
            decision = dmnEngine.parseDecision(dmnId, inputStream);
        }

        return dmnEngine.evaluateDecisionTable(decision, variables);
    }
}
