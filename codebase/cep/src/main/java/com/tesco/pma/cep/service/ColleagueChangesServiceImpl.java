package com.tesco.pma.cep.service;

import com.tesco.pma.cep.configuration.CEPFeedsProperties;
import com.tesco.pma.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.domain.DeliveryMode;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.InvalidPayloadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ColleagueChangesServiceImpl implements ColleagueChangesService {

    private final CEPFeedsProperties cepFeedsProperties;

    /**
     *
     * @param cepFeedsProperties
     */
    public ColleagueChangesServiceImpl(CEPFeedsProperties cepFeedsProperties) {
        this.cepFeedsProperties = cepFeedsProperties;
    }

    @Override
    public void processColleagueChangeEvent(String feedId,
                                            ColleagueChangeEventPayload colleagueChangeEventPayload) {
        // TODO Implement a logic related with invalidation caches of Profile API, Organisation API, PM API, ...

        DeliveryMode feedDeliveryMode = resolveDeliveryModeByFeedId(feedId);

        log.info(String.format("Processing colleague change event %s for feed delivery mode %s",
                colleagueChangeEventPayload, feedDeliveryMode));
    }

    private DeliveryMode resolveDeliveryModeByFeedId(String feedId) {
        String key = cepFeedsProperties.getFeeds().entrySet().stream()
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
