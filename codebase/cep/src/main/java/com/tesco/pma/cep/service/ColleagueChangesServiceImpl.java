package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.DeliveryMode;
import com.tesco.pma.cep.domain.EventType;
import com.tesco.pma.cep.flow.ColleagueChangesFlowEvents;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.request.ChangeAccountStatusRequest;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.service.UserManagementService;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.cep.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    private final CEPSubscribeProperties cepSubscribeProperties;
    private final ProfileService profileService;
    private final UserManagementService userManagementService;
    private final EventSender eventSender;

    private static final String FLOW_PARAMETERS_COLLEAGUE_UUID = "COLLEAGUE_UUID";

    @Override
    public void processColleagueChangeEvent(String feedId,
                                            ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ...

        DeliveryMode feedDeliveryMode = resolveDeliveryModeByFeedId(feedId);

        log.info(String.format("Processing colleague change event %s for feed delivery mode %s",
                colleagueChangeEventPayload, feedDeliveryMode));

        // For all event types except for Joiner profile must be existing
        if (!EventType.JOINER.equals(colleagueChangeEventPayload.getEventType())) {
            Optional<ColleagueProfile> optionalColleagueProfile = profileService.findProfileByColleagueUuid(
                    colleagueChangeEventPayload.getColleagueUuid());
            if (optionalColleagueProfile.isEmpty()) {
                log.error(LogFormatter.formatMessage(COLLEAGUE_NOT_FOUND, "Colleague '{}' not found"),
                        colleagueChangeEventPayload.getColleagueUuid());
                return;
            }
        }

        var updated = 0;

        switch (colleagueChangeEventPayload.getEventType()) {
            case JOINER:
                updated = processJoinerEventType(colleagueChangeEventPayload);
                break;
            case LEAVER:
                updated = processLeaverEventType(colleagueChangeEventPayload);
                break;
            case MOVER:
                updated = processMoverEventType(colleagueChangeEventPayload);
                break;
            case REINSTATEMENT:
                updated = processReinstatementEventType(colleagueChangeEventPayload);
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
        int updated = profileService.saveColleague(colleagueChangeEventPayload.getColleagueUuid());

        // Add new account
        if (updated > 0) {
            createAccount(colleagueChangeEventPayload.getColleagueUuid());
        }

        // Send event to Camunda
        if (updated > 0) {
            sendEvent(colleagueChangeEventPayload.getColleagueUuid());
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
        return 1;
    }

    // TODO If logic different from main flow
    private int processMoverEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        return profileService.updateColleague(colleagueChangeEventPayload.getColleagueUuid(),
                colleagueChangeEventPayload.getChangedAttributes());
    }

    // TODO If logic different from main flow
    private int processReinstatementEventType(ColleagueChangeEventPayload colleagueChangeEventPayload) {
        return profileService.updateColleague(colleagueChangeEventPayload.getColleagueUuid(),
                colleagueChangeEventPayload.getChangedAttributes());
    }

    private DeliveryMode resolveDeliveryModeByFeedId(String feedId) {
        String key = cepSubscribeProperties.getFeeds().entrySet().stream()
                .filter(entry -> entry.getValue().equals(feedId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (Objects.isNull(key)) {
            String message = String.format("Invalid feedId = '%s' was received from CEP", feedId);
            log.warn(message);
            throw new InvalidPayloadException(ErrorCodes.EVENT_PAYLOAD_ERROR.getCode(), message, "feedId");
        } else {
            return DeliveryMode.valueOf(key.toUpperCase());
        }
    }

    private void createAccount(UUID colleagueUuid) {
        var colleague = profileService.getColleague(colleagueUuid);
        if (colleague == null) {
            return;
        }

        CreateAccountRequest request = new CreateAccountRequest();
        request.setName(colleague.getIamId());
        request.setIamId(colleague.getIamId());
        request.setType(AccountType.USER);
        request.setStatus(AccountStatus.ENABLED);

        userManagementService.createAccount(request);
    }

    private void sendEvent(UUID colleagueUuid) {
        var event = new EventSupport(ColleagueChangesFlowEvents.PM_CEP_JOINER_EVENT_TYPE_RECEIVED);
        Map<String, Serializable> properties = new HashMap<>();
        properties.put(FLOW_PARAMETERS_COLLEAGUE_UUID, colleagueUuid);
        event.setEventProperties(properties);
        eventSender.sendEvent(event);
    }

}
