package com.tesco.pma.feedback.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A POJO for the Feedback entity and DTO.
 */
@Data
public class Feedback implements Serializable {

    private Long id;

    /**
     * Author. Colleague from whom feedback is written or requested.
     */
    @NotNull
    @Schema(description = "Author. Colleague from whom feedback is written or requested.", required = true)
    private UUID colleagueUuid;

    /**
     * Target type: COLLEAGUE, GOAL, OBJECTIVE, VALUE_BEHAVIOR, OTHER.
     */
    @NotNull
    @Schema(description = "Target type: COLLEAGUE, GOAL, OBJECTIVE, VALUE_BEHAVIOR, OTHER.", required = true)
    private FeedbackTargetType targetType;

    /**
     * Object identifier that is feedback given on/to or requested about.
     */
    @NotNull
    @Schema(description = "Object identifier that is feedback given on/to or requested about/from.", required = true)
    private String targetId;

    /**
     * Checkbox. False for Unread feedback.
     */
    @Schema(description = "Checkbox. False for Unread feedback.", required = true, defaultValue = "false")
    private Boolean read;

    /**
     * Status: DRAFT, SUBMITTED, PENDING, PENDING_DRAFT, COMPLETED.
     */
    @NotNull
    @Schema(description = "Status: DRAFT, SUBMITTED, PENDING, PENDING_DRAFT, COMPLETED.", required = true)
    private FeedbackStatus status;

    @Schema(defaultValue = "now")
    private Instant createdTime;

    private Instant modifiedTime;

    private Set<FeedbackItem> feedbackItems = new HashSet<>();
}
