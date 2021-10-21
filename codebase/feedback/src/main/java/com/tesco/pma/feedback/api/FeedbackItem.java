package com.tesco.pma.feedback.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A POJO for the FeedbackItem entity and DTO.
 */
@Data
@Schema(description = "The Feedback Item. Answers for questions, comments or free form feedback.")
public class FeedbackItem implements Serializable {

    private Long id;

    /**
     * Item code. For example, whatWellDone, whatFocusOn, comment or question1, etc.
     */
    @NotNull
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
    @NotNull
    private Long feedbackId;
}
