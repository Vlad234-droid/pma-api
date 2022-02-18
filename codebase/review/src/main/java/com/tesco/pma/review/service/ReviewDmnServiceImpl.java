package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;

@Service
@RequiredArgsConstructor
public class ReviewDmnServiceImpl implements ReviewDmnService {
    private final ProcessEngine processEngine;

    private static final String REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID = "review_allowed_prev_statuses_table";
    private static final String TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID = "tl_point_allowed_prev_statuses_table";
    private static final String TL_POINT_STATUS_TO_EVENT_TABLE_ID = "tl_point_status_to_event_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String STATUS_VAR_KEY = "STATUS";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    @Override
    public Collection<PMTimelinePointStatus> getReviewAllowedStatuses(PMReviewType reviewType, String operation) {
        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(OPERATION_VAR_KEY, operation);

        return getTimelinePointStatus(REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_ID,
                variables
        );
    }

    @Override
    public Collection<PMTimelinePointStatus> getReviewAllowedPrevStatuses(PMReviewType reviewType, PMTimelinePointStatus newStatus) {
        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        return getTimelinePointStatus(REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables
        );
    }

    @Override
    public Collection<PMTimelinePointStatus> getTlPointAllowedPrevStatuses(PMTimelinePointStatus newStatus) {
        var variables = Variables.createVariables()
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        return getTimelinePointStatus(TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables
        );
    }

    @Override
    public String getEventName(PMTimelinePointStatus status) {
        var decisionService = processEngine.getDecisionService();
        var variables = Variables.createVariables()
                .putValue(STATUS_VAR_KEY, status.getCode());

        var result =
                decisionService.evaluateDecisionTableByKey(TL_POINT_STATUS_TO_EVENT_TABLE_ID, variables);

        return result.getSingleEntry();
    }

    public static Collection<PMTimelinePointStatus> toList(List<Object> objectList) {
        return objectList.stream()
                .map(s -> getEnumIgnoreCase(PMTimelinePointStatus.class, (String) s))
                .collect(Collectors.toList());
    }

    private Collection<PMTimelinePointStatus> getTimelinePointStatus(String dmnId, VariableMap variables) {
        var decisionService = processEngine.getDecisionService();

        var result =
                decisionService.evaluateDecisionTableByKey(dmnId, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }
}
