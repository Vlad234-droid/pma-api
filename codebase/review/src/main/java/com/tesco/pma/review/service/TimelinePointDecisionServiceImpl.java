package com.tesco.pma.review.service;

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
public class TimelinePointDecisionServiceImpl implements TimelinePointDecisionService {
    private final ProcessEngine processEngine;

    private static final String TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID = "tl_point_allowed_prev_statuses_table";
    private static final String TL_POINT_STATUS_TO_EVENT_TABLE_ID = "tl_point_status_to_event_table";
    private static final String STATUS_VAR_KEY = "STATUS";
    private static final String NEW_STATUS_VAR_KEY = "NEW_STATUS";

    @Override
    public Collection<PMTimelinePointStatus> getTlPointAllowedPrevStatuses(PMTimelinePointStatus newStatus) {
        var variables = Variables.createVariables()
                .putValue(NEW_STATUS_VAR_KEY, newStatus.getCode());

        return getTimelinePointStatus(TL_POINT_ALLOWED_PREV_STATUSES_TABLE_ID,
                variables,
                processEngine
        );
    }

    @Override
    public String getStatusUpdateEventName(PMTimelinePointStatus status) {
        var decisionService = processEngine.getDecisionService();
        var variables = Variables.createVariables()
                .putValue(STATUS_VAR_KEY, status.getCode());

        var result =
                decisionService.evaluateDecisionTableByKey(TL_POINT_STATUS_TO_EVENT_TABLE_ID, variables);

        return result.getSingleEntry();
    }

    static Collection<PMTimelinePointStatus> toList(List<Object> objectList) {
        return objectList.stream()
                .map(s -> getEnumIgnoreCase(PMTimelinePointStatus.class, (String) s))
                .collect(Collectors.toList());
    }

    static Collection<PMTimelinePointStatus> getTimelinePointStatus(String dmnId, VariableMap variables, ProcessEngine processEngine) {
        var decisionService = processEngine.getDecisionService();

        var result =
                decisionService.evaluateDecisionTableByKey(dmnId, variables);

        return toList(result.collectEntries(STATUS_VAR_KEY));
    }
}
