package com.tesco.pma.flow.notifications;

public enum NotificationEvents {

    NF_PM_REVIEW_SUBMITTED(NotificationTypes.REVIEW),
    NF_PM_REVIEW_APPROVED(NotificationTypes.REVIEW),
    NF_PM_REVIEW_DECLINED(NotificationTypes.REVIEW),
    NF_PM_REVIEW_BEFORE_START(NotificationTypes.REVIEW),
    NF_PM_REVIEW_BEFORE_END(NotificationTypes.REVIEW),

    NF_ORGANISATION_OBJECTIVES(NotificationTypes.OBJECTIVES),
    NF_OBJECTIVES_APPROVED_FOR_SHARING(NotificationTypes.OBJECTIVES),
    NF_OBJECTIVE_SHARING_START(NotificationTypes.OBJECTIVES),
    NF_OBJECTIVE_SHARING_END(NotificationTypes.OBJECTIVES),

    NF_FEEDBACK_GIVEN(NotificationTypes.FEEDBACK),
    NF_FEEDBACK_REQUESTS_RESPONDED(NotificationTypes.FEEDBACK),
    NF_FEEDBACK_REQUESTED(NotificationTypes.FEEDBACK),

    NF_BEFORE_CYCLE_START(NotificationTypes.CYCLE),
    NF_BEFORE_CYCLE_END(NotificationTypes.CYCLE),
    NF_START_TIMELINE_NOTIFICATION(NotificationTypes.CYCLE),

    NF_TIPS_RECEIVED(NotificationTypes.TIPS);

    private NotificationTypes type;

    NotificationEvents(NotificationTypes type) {
        this.type = type;
    }

    public NotificationTypes getType() {
        return type;
    }

}
