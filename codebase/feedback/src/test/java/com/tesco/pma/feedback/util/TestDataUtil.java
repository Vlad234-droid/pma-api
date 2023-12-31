package com.tesco.pma.feedback.util;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.api.FeedbackTargetType;

import java.util.UUID;

public final class TestDataUtil {

    public static final UUID FEEDBACK_UUID_UNREAD = UUID.fromString("b5eedbaa-d313-449e-880e-640e6446304a");
    public static final UUID FEEDBACK_UUID_LAST = UUID.fromString("7ed4dd94-143d-4e66-93b9-1f9cd0f3a1fd");
    public static final UUID COLLEAGUE_UUID = UUID.fromString("be245be1-1f43-4d5f-85dc-db6e2cce0c2a");
    public static final UUID TARGET_COLLEAGUE_UUID = UUID.fromString("bc7e9e13-4e6a-46c2-bc67-af03fc3afd2e");
    public static final String TARGET_UUID = "cb1b76c1-31f6-45c3-a783-034ae7aed871";
    public static final UUID FEEDBACK_ITEM_UUID = UUID.fromString("693b16a7-e151-486a-b4cd-3b377dddac95");
    public static final String FEEDBACK_ITEM_CODE = "code";
    public static final String FEEDBACK_ITEM_CONTENT = "Content";
    public static final Integer FEEDBACKS_COUNT = 5;
    public static final String FIRST_NAME = "O'test";

    private TestDataUtil() {
    }

    public static Feedback buildFeedback() {
        Feedback feedback = new Feedback();
        feedback.setColleagueUuid(COLLEAGUE_UUID);
        feedback.setTargetColleagueUuid(TARGET_COLLEAGUE_UUID);
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setTargetType(FeedbackTargetType.OTHER);
        feedback.setTargetId(TARGET_UUID);
        feedback.setRead(false);
        return feedback;
    }

    public static FeedbackItem buildFeedbackItem() {
        FeedbackItem feedbackItem = new FeedbackItem();
        feedbackItem.setCode(FEEDBACK_ITEM_CODE);
        feedbackItem.setContent(FEEDBACK_ITEM_CONTENT);
        feedbackItem.setFeedbackUuid(FEEDBACK_UUID_UNREAD);
        return feedbackItem;
    }
}
