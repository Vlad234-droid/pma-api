package com.tesco.pma.cep.cfapi.v2.service;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.cfapi.v2.domain.EventType;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.tesco.pma.cep.cfapi.v2.domain.EventType.DELETION;
import static com.tesco.pma.cep.cfapi.v2.domain.EventType.JOINER;
import static com.tesco.pma.cep.cfapi.v2.domain.EventType.LEAVER;
import static com.tesco.pma.cep.cfapi.v2.domain.EventType.MODIFICATION;
import static com.tesco.pma.cep.cfapi.v2.exception.ErrorCodes.CHANGED_ATTRIBUTES_NOT_FOUND;
import static com.tesco.pma.cep.cfapi.v2.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;
import static com.tesco.pma.colleague.security.exception.ErrorCodes.SECURITY_ACCOUNT_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";

    private final ProfileService profileService;
    private final UserManagementService userManagementService;
    private final EventSender eventSender;
    private final NamedMessageSourceAccessor messages;

    private static final EnumMap<EventType, ToIntFunction<ColleagueChangeEventPayload>> PROCESSORS
            = new EnumMap<>(EventType.class);

    @PostConstruct
    public void init() {
        PROCESSORS.put(JOINER, this::processJoinerEventType);
        PROCESSORS.put(LEAVER, this::processLeaverEventType);
        PROCESSORS.put(MODIFICATION, this::processModificationEventType);
        PROCESSORS.put(DELETION, this::processDeletionEventType);
    }

    @Override
    public void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ... //NOSONAR

        // Now support only Joiner, Leaver, Modification, Deletion event types
        if (!EnumSet.of(JOINER, LEAVER, MODIFICATION, DELETION).contains(colleagueChangeEventPayload.getEventType())) {
            return;
        }

        // For all event types except for Joiner and Modification profile must be existing
        if (EnumSet.of(LEAVER, DELETION).contains(colleagueChangeEventPayload.getEventType())) {
            Optional<ColleagueProfile> optionalColleagueProfile = profileService.findProfileByColleagueUuid(
                    colleagueChangeEventPayload.getColleagueUuid());
            if (optionalColleagueProfile.isEmpty()) {
                log.error(LogFormatter.formatMessage(COLLEAGUE_NOT_FOUND, "Colleague '{}' not found"),
                        colleagueChangeEventPayload.getColleagueUuid());
                return;
            }
        }

        startProcessColleagueChangeEvent(colleagueChangeEventPayload);
    }

    private void startProcessColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        log.debug("Start processing of event type " + colleagueChangeEventPayload.getEventType());

        var processor = PROCESSORS.get(colleagueChangeEventPayload.getEventType());
        if (processor == null) {
            throw new IllegalArgumentException("Invalid event type " + colleagueChangeEventPayload.getEventType());
        }

        var updated = processor.applyAsInt(colleagueChangeEventPayload);
        if (updated == 0) {
            log.warn(LogFormatter.formatMessage(COLLEAGUE_NOT_FOUND, "For colleague '{}' was not updated records"),
                    colleagueChangeEventPayload.getColleagueUuid());
        }
    }

    private int processJoinerEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // Send an event to Camunda
        sendAssignmentEvent(colleagueChangeEventPayload.getColleagueUuid(), colleagueChangeEventPayload.getCurrent());
        return 1;
    }

    private int processLeaverEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // Disable an access a colleague to app
        var account = userManagementService.findAccountByColleagueUuid(colleagueChangeEventPayload.getColleagueUuid());
        if (account == null) {
            String message = String.format("An account for colleague with uuid = %s not found",
                    colleagueChangeEventPayload.getColleagueUuid());
            log.error(LogFormatter.formatMessage(SECURITY_ACCOUNT_NOT_FOUND, message));
            return 0;
        }

        var changeAccountStatusRequest = new ChangeAccountStatusRequest();
        changeAccountStatusRequest.setName(account.getName());
        changeAccountStatusRequest.setStatus(AccountStatus.DISABLED);
        userManagementService.changeAccountStatus(changeAccountStatusRequest);

        // Send event to Camunda
        sendEvent(colleagueChangeEventPayload.getColleagueUuid(), EventNames.CEP_COLLEAGUE_LEFT);

        return 1;
    }

    private int processModificationEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        Optional<ColleagueProfile> optionalColleagueProfile = profileService.findProfileByColleagueUuid(
                colleagueChangeEventPayload.getColleagueUuid());
        if (optionalColleagueProfile.isEmpty()) {
            return processJoinerEventType(colleagueChangeEventPayload);
        }

        var changedAttributes = filteringChangedAttributes(colleagueChangeEventPayload);
        if (changedAttributes.isEmpty()) {
            log.warn(LogFormatter.formatMessage(messages, CHANGED_ATTRIBUTES_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueChangeEventPayload.getColleagueUuid())));
            return 1;
        }

        // Send event to Camunda
        sendUpdateEvent(colleagueChangeEventPayload.getColleagueUuid(), colleagueChangeEventPayload.getCurrent());

        return 1;
    }

    private int processDeletionEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        return processLeaverEventType(colleagueChangeEventPayload);
    }

    private Collection<String> filteringChangedAttributes(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        var changedAttributes = colleagueChangeEventPayload.getChangedAttributes();
        if (changedAttributes.isEmpty()) {
            return changedAttributes;
        }

        var supportedAttributes = profileService.getColleagueFactsAPISupportedAttributes();

        return supportedAttributes.stream()
                .filter(changedAttributes::contains)
                .collect(Collectors.toList());
    }

    private void sendEvent(UUID colleagueUuid, EventNames eventName) {
        var event = new EventSupport(eventName);
        event.setEventProperties(Map.of(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid));
        eventSender.sendEvent(event);
    }

    private void sendUpdateEvent(UUID colleagueUuid, Colleague colleague) {
        var event = new EventSupport(EventNames.CEP_COLLEAGUE_UPDATED);
        var properties = Map
                .of(FlowParameters.COLLEAGUE.name(), colleague,
                        FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid);
        event.setEventProperties(properties);
        eventSender.sendEvent(event);
    }

    private void sendAssignmentEvent(UUID colleagueUuid, Colleague colleague) {
        var event = new EventSupport(EventNames.PM_COLLEAGUE_CYCLE_ASSIGNMENT_NEW_JOINER);
        var properties = Map
                .of(FlowParameters.COLLEAGUE.name(), colleague,
                        FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid);
        event.setEventProperties(properties);
        eventSender.sendEvent(event);
    }

}
