package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.Event;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-19 23:23
 */

public abstract class AbstractInitNotificationHandler extends CamundaAbstractFlowHandler {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    private final Map<FlowParameters, FlowParameters> colleaguesFlowParams = Map.of(
            FlowParameters.COLLEAGUE_UUID, FlowParameters.COLLEAGUE_PROFILE,
            FlowParameters.SOURCE_COLLEAGUE_UUID, FlowParameters.SOURCE_COLLEAGUE_PROFILE,
            FlowParameters.SENDER_COLLEAGUE_UUID, FlowParameters.SENDER_COLLEAGUE_PROFILE);

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var event = context.getEvent();
        context.setVariable(FlowParameters.EVENT_NAME, event.getEventName());

        processColleagues(event, context);
        context.setVariable(FlowParameters.IS_MANAGER, isManager(context.getVariable(FlowParameters.COLLEAGUE_PROFILE)));
    }

    protected void processColleagues(Event event, ExecutionContext context) {
        for (Map.Entry<FlowParameters, FlowParameters> param : colleaguesFlowParams.entrySet()) {
            var colleagueUUID = (UUID) event.getEventProperty(param.getKey().name());

            if (colleagueUUID == null) {
                continue;
            }

            var colleagueProfile = getColleagueProfile(colleagueUUID);
            context.setVariable(param.getValue(), colleagueProfile);
        }
    }

    protected boolean isManager(ColleagueProfile colleagueProfile) {
        return colleagueProfile.getColleague().getWorkRelationships().get(0).getIsManager();
    }

    protected ColleagueProfile getColleagueProfile(UUID colleagueUUID) {
        return profileService.findProfileByColleagueUuid(colleagueUUID)
                .orElseThrow(() -> notFound(ErrorCodes.PROFILE_NOT_FOUND, "UUID", colleagueUUID.toString()));
    }

    private AbstractApiRuntimeException notFound(ErrorCodes errorCode, String paramName, String paramValue) {
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }

}