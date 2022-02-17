package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewDmnServiceImpl implements ReviewDmnService {
    private final ProcessEngine processEngine;

    private static final String REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_KEY = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID = "review_allowed_prev_statuses_table";
    private static final String TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID = "tl_point_allowed_prev_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String STATUS_VAR_KEY = "STATUS";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    @Override
    public List<PMTimelinePointStatus> getReviewAllowedStatuses(PMReviewType reviewType, String operation) {
        var decisionService = processEngine.getDecisionService();

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(OPERATION_VAR_KEY, operation);

        var result =
                decisionService.evaluateDecisionTableByKey(REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_KEY, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }

    @Override
    public List<PMTimelinePointStatus> getReviewAllowedPrevStatuses(PMReviewType reviewType, PMTimelinePointStatus newStatus) {
        var decisionService = processEngine.getDecisionService();

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        var result =
                decisionService.evaluateDecisionTableByKey(REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }

    @Override
    public List<PMTimelinePointStatus> getTlPointAllowedPrevStatuses(PMTimelinePointStatus newStatus) {
        var decisionService = processEngine.getDecisionService();

        var variables = Variables.createVariables()
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        var result =
                decisionService.evaluateDecisionTableByKey(TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }

    private List<PMTimelinePointStatus> toList(List<Object> objectList) {
        return objectList.stream()
                .map(s -> PMTimelinePointStatus.valueOf((String) s))
                .collect(Collectors.toList());
    }
}
