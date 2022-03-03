package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.tesco.pma.review.service.TimelinePointDecisionServiceImpl.getTimelinePointStatus;

@Service
@RequiredArgsConstructor
public class ReviewDecisionServiceImpl implements ReviewDecisionService {
    private final ProcessEngine processEngine;

    private static final String REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_ID = "review_operation_allowed_statuses_table";
    private static final String REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID = "review_allowed_prev_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    @Override
    public Collection<PMTimelinePointStatus> getReviewAllowedStatuses(PMReviewType reviewType, String operation) {
        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(OPERATION_VAR_KEY, operation);

        return getTimelinePointStatus(REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_ID,
                variables,
                processEngine
        );
    }

    @Override
    public Collection<PMTimelinePointStatus> getReviewAllowedPrevStatuses(PMReviewType reviewType, PMTimelinePointStatus newStatus) {
        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        return getTimelinePointStatus(REVIEW_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables,
                processEngine
        );
    }
}
