package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AbstractApiRuntimeException;
import com.tesco.pma.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class ExtractProfileAttributeHandler extends CamundaAbstractFlowHandler {

    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var attributeName = context.getVariable(FlowParameters.PROFILE_ATTRIBUTE_NAME);
        var colleagueUUID = (UUID) context.getVariable(FlowParameters.COLLEAGUE_UUID);

        var result = profileService.findProfileByColleagueUuid(colleagueUUID)
                .orElseThrow(() -> notFound(ErrorCodes.PROFILE_NOT_FOUND, "UUID", colleagueUUID.toString()))
                .getProfileAttributes().stream()
                .filter(attr -> attributeName.equals(attr.getName()))
                .findFirst()
                .orElseThrow(() -> notFound(ErrorCodes.PROFILE_ATTRIBUTE_NOT_FOUND, "Profile attribute", colleagueUUID.toString()));

        context.setVariable(FlowParameters.PROFILE_ATTRIBUTE_VALUE, result.getValue());
    }

    private AbstractApiRuntimeException notFound(ErrorCodes errorCode, String paramName, String paramValue){
        return new NotFoundException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode,
                        Map.of("param_name", paramName, "param_value", paramValue)));
    }
}
