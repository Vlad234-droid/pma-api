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
public class InitObjectiveNotificationHandler extends InitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        context.setVariable(FlowParameters.COLLEAGUE_WORK_LEVEL,
                getWorkLevel(context.getVariable(FlowParameters.COLLEAGUE_PROFILE)));
    }

    protected WorkLevel getWorkLevel(ColleagueProfile colleagueProfile) {
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getWorkLevel();
    }

}