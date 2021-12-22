package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.event.Event;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Component
public class InitReminderNotificationHandler extends InitNotificationHandler {

    @Autowired
    private ReviewService reviewService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        var colleagueProfile = ((ColleagueProfile) context.getVariable(FlowParameters.COLLEAGUE_PROFILE));

        context.setVariable(FlowParameters.COLLEAGUE_REMINDERS,
                getReminders(colleagueProfile.getColleague().getColleagueUUID()));
    }

    protected String getReminders(UUID colleagueUUID) {
        var timelinePoints = reviewService.getCycleTimelineByColleague(colleagueUUID);

        return timelinePoints.stream()
                .filter(tp -> PMElementType.TIMELINE_POINT.equals(tp.getType()))
                .map(TimelinePoint::getCode)
                .collect(Collectors.joining(";"));
    }

}