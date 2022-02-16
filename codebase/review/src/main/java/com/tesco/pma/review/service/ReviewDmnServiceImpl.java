package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewDmnServiceImpl implements ReviewDmnService {
    private final ProcessEngine processEngine;

    private static final String REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_KEY = "review_operation_allowed_statuses_table";
    private static final String REVIEW_TYPE_VAR_KEY = "REVIEW_TYPE";
    private static final String OPERATION_VAR_KEY = "OPERATION";
    private static final String STATUS_VAR_KEY = "STATUS";

    @Override
    public List<String> getReviewAllowedStatuses(PMReviewType reviewType, String operation) {
        var decisionService = processEngine.getDecisionService();

        var variables = Variables.createVariables()
                .putValue(REVIEW_TYPE_VAR_KEY, reviewType.getCode())
                .putValue(OPERATION_VAR_KEY, operation);

        var result =
                decisionService.evaluateDecisionTableByKey(REVIEW_OPERATION_ALLOWED_STATUSES_TABLE_KEY, variables);

        return result.collectEntries(STATUS_VAR_KEY);
    }
}
