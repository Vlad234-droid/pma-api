package com.tesco.pma.feedback.validator;

import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.feedback.api.Feedback;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

import static com.tesco.pma.rest.HttpStatusCodes.BAD_REQUEST;

/**
 * Utility class for feedback validation
 */
@Slf4j
@UtilityClass
public class FeedbackValidator {

    /**
     * Validate feedback's colleague
     * @param feedback a Feedback to validate its colleague
     * @param colleagueUuid an identifier of feedback's colleague or target colleague
     * @return true - if feedback is valid
     * @throws InvalidPayloadException if feedback is invalid
     */
    public boolean validateFeedbackColleague(Feedback feedback, UUID colleagueUuid) {
        if (Objects.equals(feedback.getColleagueUuid(), feedback.getTargetColleagueUuid())) {
            throw new InvalidPayloadException(BAD_REQUEST,
                    "Feedback's Colleague UUID must not be same as Target Colleague UUID", "feedback.colleagueUuid");
        }
        if (!Objects.equals(feedback.getColleagueUuid(), colleagueUuid)
                && !Objects.equals(feedback.getTargetColleagueUuid(), colleagueUuid)) {
            throw new InvalidPayloadException(BAD_REQUEST,
                    "Colleague UUID from token must be same as Feedback's Colleague UUID or Target Colleague", "feedback.colleagueUuid");
        }
        return true;
    }
}