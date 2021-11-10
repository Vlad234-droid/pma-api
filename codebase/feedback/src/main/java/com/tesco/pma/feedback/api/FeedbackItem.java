package com.tesco.pma.feedback.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * A POJO for the FeedbackItem entity and DTO.
 */
@Data
@Schema(description = "The Feedback Item. Answers for questions, comments or free form feedback.")
public class FeedbackItem {

    private UUID uuid;

    /**
     * Item code. For example, whatWellDone, whatFocusOn, comment or question1, etc.
     */
    @Schema(description = "Item code. For example, whatWellDone, whatFocusOn, comment or question1, etc.", required = true)
    private String code;

    /**
     * Body of feedback item. Answer for question or comment.
     */
    @Size(max = 500)
    @Schema(description = "Body of feedback item. Answer for question or comment.")
    private String content;

    /**
     * Link to feedback.
     */
    @Schema(description = "Link to feedback", required = true)
    private UUID feedbackUuid;
}
