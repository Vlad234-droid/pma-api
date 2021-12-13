package com.tesco.pma.flow.handlers;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:56
 */
public enum FlowParameters {
    EVENT_NAME,
    REVIEW_TYPE,
    REVIEW_UUID,
    IS_MANAGER,
    SEND,

    PM_CYCLE_UUID,
    PM_CYCLE,
    PM_CYCLE_START_TIME,

    //timeline variables
    START_DATE,
    BEFORE_START_DATE,
    BEFORE_END_DATE,
    END_DATE,

    COLLEAGUE_UUID,

    PROFILE_ATTRIBUTE_NAME,
    PROFILE_ATTRIBUTE_VALUE,

    //notifications
    IS_NOTIFICATION_ALLOWED,
    NOTIFICATION_TEMPLATE_ID
}
