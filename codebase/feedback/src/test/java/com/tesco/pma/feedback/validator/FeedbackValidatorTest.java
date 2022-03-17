package com.tesco.pma.feedback.validator;

import com.tesco.pma.exception.InvalidPayloadException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.tesco.pma.feedback.util.TestDataUtil.TARGET_COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.buildFeedback;
import static com.tesco.pma.rest.HttpStatusCodes.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackValidatorTest {

    @Test
    void validateFeedbackColleague() {
        var feedback = buildFeedback();
        feedback.setColleagueUuid(COLLEAGUE_UUID);
        feedback.setTargetColleagueUuid(TARGET_COLLEAGUE_UUID);

        assertTrue(FeedbackValidator.validateFeedbackColleague(feedback, COLLEAGUE_UUID));
    }

    @Test
    void validateFeedbackColleagueUnsuccessIfColleagueIsSameAsTargetColleagueOfFeedback() {
        var invalidFeedback = buildFeedback();
        invalidFeedback.setColleagueUuid(COLLEAGUE_UUID);
        invalidFeedback.setTargetColleagueUuid(COLLEAGUE_UUID);

        final var exception = assertThrows(InvalidPayloadException.class,
                () -> FeedbackValidator.validateFeedbackColleague(invalidFeedback, COLLEAGUE_UUID));

        assertEquals(BAD_REQUEST, exception.getCode());
        assertEquals("Feedback's Colleague UUID must not be same as Target Colleague UUID", exception.getMessage());
    }

    @Test
    void validateFeedbackColleagueUnsuccessIfCurrentColleagueIsNoneOfColleaguesOfFeedback() {
        var invalidFeedback = buildFeedback();
        invalidFeedback.setColleagueUuid(UUID.randomUUID());
        invalidFeedback.setTargetColleagueUuid(UUID.randomUUID());

        final var exception = assertThrows(InvalidPayloadException.class,
                () -> FeedbackValidator.validateFeedbackColleague(invalidFeedback, COLLEAGUE_UUID));

        assertEquals(BAD_REQUEST, exception.getCode());
        assertEquals("Colleague UUID from token must be same as Feedback's Colleague UUID or Target Colleague", exception.getMessage());
    }
}