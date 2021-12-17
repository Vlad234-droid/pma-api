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
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.event.Event;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-19 23:23
 */
@Slf4j
@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InitReviewNotification extends CamundaAbstractFlowHandler {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Autowired
    private ReviewService reviewService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var event = context.getEvent();
        var eventName = event.getEventName();
        var colleagueUUID = getColleagueUUID(event);
        var colleagueProfile = getColleagueProfile(getColleagueUUID(event));

        context.setVariable(FlowParameters.EVENT_NAME, eventName);
        context.setVariable(FlowParameters.COLLEAGUE_PROFILE, colleagueProfile);
        context.setVariable(FlowParameters.REVIEW_TYPE, getReviewType(event));
        context.setVariable(FlowParameters.IS_MANAGER, isManager(colleagueProfile));
        context.setVariable(FlowParameters.COLLEAGUE_WORK_LEVEL, getWorkLevel(colleagueProfile));
        context.setVariable(FlowParameters.COLLEAGUE_REMINDERS, getReminders(colleagueUUID));
    }

    public boolean isManager(ColleagueProfile colleagueProfile) {
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getIsManager();
    }

    public PMReviewType getReviewType(Event event) {
        return (PMReviewType) event.getEventProperty(FlowParameters.REVIEW_TYPE.name());
    }

    public UUID getColleagueUUID(Event event) {
        return (UUID) event.getEventProperty(FlowParameters.COLLEAGUE_UUID.name());
    }

    public ColleagueProfile getColleagueProfile(UUID colleagueUUID) {
        return profileService.findProfileByColleagueUuid(colleagueUUID)
                .orElseThrow(() -> notFound(ErrorCodes.PROFILE_NOT_FOUND, "UUID", colleagueUUID.toString()));
    }

    public WorkLevel getWorkLevel(ColleagueProfile colleagueProfile){
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getWorkLevel();
    }

    public String getReminders(UUID colleagueUUID) {
        var timelinePoints = reviewService.getCycleTimelineByColleague(colleagueUUID);

        return timelinePoints.stream()
                .filter(tp -> PMElementType.TIMELINE_POINT.equals(tp.getType()))
                .map(TimelinePoint::getCode)
                .collect(Collectors.joining(";"));
    }

    private AbstractApiRuntimeException notFound(ErrorCodes errorCode, String paramName, String paramValue) {
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}