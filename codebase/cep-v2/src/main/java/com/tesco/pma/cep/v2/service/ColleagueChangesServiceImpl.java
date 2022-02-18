package com.tesco.pma.cep.v2.service;

import com.tesco.pma.cep.v2.configuration.ColleagueFactsApiProperties;
import com.tesco.pma.cep.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.cep.v2.domain.EventType.DELETION;
import static com.tesco.pma.cep.v2.domain.EventType.JOINER;
import static com.tesco.pma.cep.v2.domain.EventType.LEAVER;
import static com.tesco.pma.cep.v2.domain.EventType.MODIFICATION;
import static com.tesco.pma.cep.v2.exception.ErrorCodes.CHANGED_ATTRIBUTES_NOT_FOUND;
import static com.tesco.pma.cep.v2.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    private final ColleagueFactsApiProperties colleagueFactsApiProperties;
    private final ProfileService profileService;
    private final UserManagementService userManagementService;
    private final EventSender eventSender;

    @Override
    public void processColleagueChangeEvent(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ...

        // Now support only Joiner, Leaver, Modification, Deletion event types
        if (!EnumSet.of(JOINER, LEAVER, MODIFICATION, DELETION).contains(colleagueChangeEventPayload.getEventType())) {
            return;
        }

        // For all event types except for Joiner profile must be existing
        if (!JOINER.equals(colleagueChangeEventPayload.getEventType())) {
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
        var updated = 0;

        switch (colleagueChangeEventPayload.getEventType()) {
            case JOINER:
                updated = processJoinerEventType(colleagueChangeEventPayload);
                break;
            case LEAVER:
                updated = processLeaverEventType(colleagueChangeEventPayload);
                break;
            case MODIFICATION:
                updated = processModificationEventType(colleagueChangeEventPayload);
                break;
            case DELETION:
                updated = processDeletionEventType(colleagueChangeEventPayload);
                break;
            default:
                throw new IllegalArgumentException("Invalid event type " + colleagueChangeEventPayload.getEventType());
        }

        if (updated == 0) {
            log.warn(LogFormatter.formatMessage(COLLEAGUE_NOT_FOUND, "For colleague '{}' was not updated records"),
                    colleagueChangeEventPayload.getColleagueUuid());

        }
    }

    private int processJoinerEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        int updated;
        if (colleagueFactsApiProperties.isForce()) {
            updated = profileService.create(colleagueChangeEventPayload.getColleagueUuid());
        } else {
            updated = profileService.create(colleagueChangeEventPayload.getCurrent());
        }
        if (updated > 0) {
            // Send an event to User Management Service on creation a new account
            sendEvent(colleagueChangeEventPayload.getColleagueUuid(), EventNames.POST_CEP_COLLEAGUE_ADDED);

            // Send an event to Camunda
            sendEvent(colleagueChangeEventPayload.getColleagueUuid(), EventNames.CEP_COLLEAGUE_ADDED);
        }
        return updated;
    }

    private int processLeaverEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // Disable an access a colleague to app
        var account = userManagementService.findAccountByColleagueUuid(colleagueChangeEventPayload.getColleagueUuid());
        var changeAccountStatusRequest = new ChangeAccountStatusRequest();
        changeAccountStatusRequest.setName(account.getName());
        changeAccountStatusRequest.setStatus(AccountStatus.DISABLED);
        userManagementService.changeAccountStatus(changeAccountStatusRequest);

        // Send event to Camunda
        sendEvent(colleagueChangeEventPayload.getColleagueUuid(), EventNames.CEP_COLLEAGUE_LEFT);

        return 1;
    }

    private int processModificationEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        var changedAttributes = filteringChangedAttributes(colleagueChangeEventPayload);
        if (changedAttributes.isEmpty()) {
            log.warn(LogFormatter.formatMessage(CHANGED_ATTRIBUTES_NOT_FOUND, "For colleague '{}' was not updated records"),
                    colleagueChangeEventPayload.getColleagueUuid());
            return 1;
        }

        int updated;
        if (colleagueFactsApiProperties.isForce()) {
            updated = profileService.updateColleague(colleagueChangeEventPayload.getColleagueUuid(),
                    colleagueChangeEventPayload.getChangedAttributes());
        } else {
            updated = profileService.updateColleague(colleagueChangeEventPayload.getCurrent());
        }

        // Send event to Camunda
        if (updated > 0) {
            sendEvent(colleagueChangeEventPayload.getColleagueUuid(), EventNames.CEP_COLLEAGUE_UPDATED);
        }

        return updated;
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
        Map<String, Serializable> properties = new HashMap<>();
        properties.put(EventParams.COLLEAGUE_UUID.name(), colleagueUuid);
        event.setEventProperties(properties);
        eventSender.sendEvent(event);
    }

}
