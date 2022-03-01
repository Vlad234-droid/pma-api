package com.tesco.pma.reporting.rating.service;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private static final String OVERALL_RATING_DECISION_TABLE_KEY = "overall_rating_table";
    private static final String WHAT_RATING_VAR_KEY = "WHAT_RATING";
    private static final String HOW_RATING_VAR_KEY = "HOW_RATING";


    private final ProcessEngine processEngine;

    @Override
    public String getOverallRating(String whatRating, String howRating) {
        var decisionService = processEngine.getDecisionService();

        var variables = Variables.createVariables()
                .putValue(WHAT_RATING_VAR_KEY, whatRating)
                .putValue(HOW_RATING_VAR_KEY, howRating);

        var result =
                decisionService.evaluateDecisionTableByKey(OVERALL_RATING_DECISION_TABLE_KEY, variables);

        return result.getSingleEntry();
    }


}
