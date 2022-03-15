package com.tesco.pma.flow.action;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Action to start a pm cycle for colleague
 * Parameters:
 *  - COLLEAGUE_UUID
 *  - PM_CYCLE_UUID
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ColleagueCycleCreateEventAction implements Action {


    private final PMColleagueCycleService pmColleagueCycleService;
    private final NamedMessageSourceAccessor messages;


    @Override
    public EventResponse perform(Event event) {
        var eventProps = event.getEventProperties();
        var pmCycleUUID = (UUID) eventProps.get(FlowParameters.PM_CYCLE_UUID.name());

        if (pmCycleUUID == null) {
            throw propNotFoundException(FlowParameters.PM_CYCLE_UUID.name());
        }

        var colleagueUUID = (UUID) eventProps.get(FlowParameters.COLLEAGUE_UUID.name());

        if (colleagueUUID == null) {
            throw propNotFoundException(FlowParameters.COLLEAGUE_UUID.name());
        }

        pmColleagueCycleService.start(pmCycleUUID, colleagueUUID);
        return new EventResponseSupport(event.getEventName(), EventResponse.END);
    }

    private NotFoundException propNotFoundException(String propertyName) {
        return new NotFoundException(ErrorCodes.PARAMETER_CANNOT_BE_READ.getCode(),
                messages.getMessage(ErrorCodes.PARAMETER_CANNOT_BE_READ, Map.of("property", propertyName)));
    }

}
