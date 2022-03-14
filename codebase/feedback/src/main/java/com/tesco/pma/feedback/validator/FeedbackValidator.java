package com.tesco.pma.feedback.validator;

import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.rest.HttpStatusCodes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for feedback validation
 */
@Slf4j
@UtilityClass
public class FeedbackValidator {

    /**
     * Validate feedback's colleague
     * @param feedback a Feedback to validate it
     * @param colleagueUuid an identifier of feedback's colleague or target colleague
     * @return true - if feedback is valid
     * @throws InvalidPayloadException if feedback is invalid
     */
    public boolean validateFeedbackColleague(Feedback feedback, UUID colleagueUuid) {
        if (Objects.equals(feedback.getColleagueUuid(), feedback.getTargetColleagueUuid())) {
            throw new InvalidPayloadException(HttpStatusCodes.BAD_REQUEST,
                    "Feedback's Colleague UUID must not be same as Target Colleague UUID", "feedback.colleagueUuid");
        }
        if (!Objects.equals(feedback.getColleagueUuid(), colleagueUuid)
                && !Objects.equals(feedback.getTargetColleagueUuid(), colleagueUuid)) {
            throw new InvalidPayloadException(HttpStatusCodes.BAD_REQUEST,
                    "Colleague UUID from token must be same as Feedback's Colleague UUID or Target Colleague", "feedback.colleagueUuid");
        }
        return true;
    }
}