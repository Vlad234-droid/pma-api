package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.dao.ProfileAttributeDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

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

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var event = context.getEvent();
        var eventName = event.getEventName();
        var colleagueProfile = getColleagueProfile(getColleagueUUID());

        context.setVariable(FlowParameters.EVENT_NAME, eventName);
        context.setVariable(FlowParameters.COLLEAGUE_PROFILE, colleagueProfile);
        context.setVariable(FlowParameters.REVIEW_TYPE, getReviewType());
        context.setVariable(FlowParameters.IS_MANAGER, isManager(colleagueProfile));

    }

    public boolean isManager(ColleagueProfile colleagueProfile) {
        //TODO handle NPE
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getIsManager();
    }

    public PMReviewType getReviewType() {
        return null;
    }

    public UUID getColleagueUUID() {
        return null;
    }

    public ColleagueProfile getColleagueProfile(UUID colleagueUUID){
        return profileService.findProfileByColleagueUuid(colleagueUUID)
                .orElseThrow(() -> notFound(ErrorCodes.PROFILE_NOT_FOUND, "UUID", colleagueUUID.toString()));
    }

    private AbstractApiRuntimeException notFound(ErrorCodes errorCode, String paramName, String paramValue){
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}