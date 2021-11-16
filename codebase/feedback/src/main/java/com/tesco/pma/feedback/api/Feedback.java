package com.tesco.pma.feedback.api;

import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A POJO for the Feedback entity and DTO.
 */
@Data
public class Feedback {

    private UUID uuid;

    /**
     * Identifier of author. Colleague from whom feedback is written or requested.
     */
    @Schema(description = "Author. Colleague from whom feedback is written or requested.", required = true)
    private UUID colleagueUuid;

    /**
     * Author. Colleague from whom feedback is written or requested.
     */
    private ColleagueProfile colleagueProfile;

    /**
     * Identifier of colleague to whom feedback is given.
     */
    @Schema(description = "Colleague to whom feedback is given.", required = true)
    private UUID targetColleagueUuid;

    /**
     * Colleague to whom feedback is given.
     */
    private ColleagueProfile targetColleagueProfile;

    /**
     * Target type: GOAL, OBJECTIVE, VALUE_BEHAVIOR, OTHER.
     */
    @Schema(description = "Target type: GOAL, OBJECTIVE, VALUE_BEHAVIOR, OTHER.")
    private FeedbackTargetType targetType;

    /**
     * Object identifier that is feedback given on/to or requested about.
     */
    @Schema(description = "Object identifier that is feedback given on/to or requested about/from.")
    private String targetId;

    /**
     * Checkbox. False for Unread feedback.
     */
    @Schema(description = "Checkbox. False for Unread feedback.", defaultValue = "false")
    private Boolean read = Boolean.FALSE;

    /**
     * Status: DRAFT, SUBMITTED, PENDING, COMPLETED.
     */
    @Schema(description = "Status: DRAFT, SUBMITTED, PENDING, COMPLETED.", required = true)
    private FeedbackStatus status;

    @Schema(defaultValue = "now")
    private Instant createdTime;

    private Instant updatedTime;

    private Set<FeedbackItem> feedbackItems = new HashSet<>();
}
