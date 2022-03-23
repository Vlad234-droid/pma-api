package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.flow.FlowParameters.COLLEAGUE_UUIDS;
import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_INVALID_COLLEAGUE_UUID_FORMAT;

/**
 * Reads colleagues by list of colleague uuids
 * Output variable COLLEAGUES: List&lt;Colleague&gt;
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-12 19:55
 */
@Component
@RequiredArgsConstructor
public class ReadColleaguesHandler extends CamundaAbstractFlowHandler {
    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messages;

    @Override
    protected void execute(ExecutionContext context) {
        var uuids = getColleagueUuids(context);
        List<Colleague> colleagues = uuids.stream().map(profileService::findProfileByColleagueUuid)
                .filter(Optional::isPresent).map(o -> o.get().getColleague())
                .collect(Collectors.toList());
        context.setVariable(FlowParameters.COLLEAGUES, colleagues);
    }

    @SuppressWarnings("unchecked")
    private List<UUID> getColleagueUuids(ExecutionContext context) {
        try {
            List<? extends Serializable> uuids = context.getNullableVariable(COLLEAGUE_UUIDS);
            if (uuids == null || uuids.isEmpty()) {
                var event = context.getEvent();
                uuids = (List<? extends Serializable>) event.getEventProperty(COLLEAGUE_UUIDS.name());
                if (uuids == null || uuids.isEmpty()) {
                    return Collections.emptyList();
                }
            }
            return uuids.stream().map(Objects::toString).map(UUID::fromString).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn(messages.getMessage(EVENT_INVALID_COLLEAGUE_UUID_FORMAT), e);
            return Collections.emptyList();
        }
    }
}
