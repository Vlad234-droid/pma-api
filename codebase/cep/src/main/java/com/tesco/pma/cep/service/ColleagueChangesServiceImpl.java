package com.tesco.pma.cep.service;

import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.DeliveryMode;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.tesco.pma.cep.exception.ErrorCodes.COLLEAGUE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    private final CEPSubscribeProperties cepSubscribeProperties;
    private final ProfileService profileService;

    @Override
    public void processColleagueChangeEvent(String feedId,
                                            ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ...

        DeliveryMode feedDeliveryMode = resolveDeliveryModeByFeedId(feedId);

        log.info(String.format("Processing colleague change event %s for feed delivery mode %s",
                colleagueChangeEventPayload, feedDeliveryMode));

        Optional<ColleagueProfile> optionalColleagueProfile = profileService.findProfileByColleagueUuid(
                colleagueChangeEventPayload.getColleagueUuid());
        if (optionalColleagueProfile.isEmpty()) {
            log.error(LogFormatter.formatMessage(COLLEAGUE_NOT_FOUND, "Colleague '{}' not found"),
                    colleagueChangeEventPayload.getColleagueUuid());
            return;
        }

        var updated = 0;

        switch (colleagueChangeEventPayload.getEventType()) {
            case JOINER:
                updated = processJoinerEventType(colleagueChangeEventPayload, feedDeliveryMode);
                break;
            case LEAVER:
                updated = processLeaverEventType(colleagueChangeEventPayload, feedDeliveryMode);
                break;
            case MOVER:
                updated = processMoverEventType(colleagueChangeEventPayload, feedDeliveryMode);
                break;
            case REINSTATEMENT:
                updated = processReinstatementEventType(colleagueChangeEventPayload, feedDeliveryMode);
                break;
            default:
                throw new IllegalArgumentException("Invalid event type " + colleagueChangeEventPayload.getEventType());
        }

        if (updated == 0) {
            // TODO
        }
    }

    private int processJoinerEventType(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                       DeliveryMode feedDeliveryMode) {
        return profileService.updateColleague(colleagueChangeEventPayload.getColleagueUuid(),
                colleagueChangeEventPayload.getChangedAttributes());
    }

    // TODO
    private int processLeaverEventType(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                       DeliveryMode feedDeliveryMode) {
        return processJoinerEventType(colleagueChangeEventPayload, feedDeliveryMode);
    }

    // TODO
    private int processMoverEventType(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                      DeliveryMode feedDeliveryMode) {
        return processJoinerEventType(colleagueChangeEventPayload, feedDeliveryMode);
    }

    // TODO
    private int processReinstatementEventType(ColleagueChangeEventPayload colleagueChangeEventPayload,
                                              DeliveryMode feedDeliveryMode) {
        return processJoinerEventType(colleagueChangeEventPayload, feedDeliveryMode);
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

}
