package com.tesco.pma.flow;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:56
 */
public enum FlowParameters {
    EVENT_NAME,
    EVENT_PARAMS,
    REVIEW_TYPE,
    IS_MANAGER,
    SEND,
    RESULT,
    PLACEHOLDERS,

    MODEL_PARENT_ELEMENT,

    PM_CYCLE_UUID,
    PM_CYCLE,
    PM_CYCLE_START_TIME,

    //timeline variables
    START_DATE,
    BEFORE_START_DATE,
    BEFORE_END_DATE,
    END_DATE,
    // formatted date strings
    START_DATE_S,
    BEFORE_START_DATE_S,
    BEFORE_END_DATE_S,
    END_DATE_S,

    COLLEAGUE_UUID,
    COLLEAGUE_PROFILE,
    COLLEAGUE_WORK_LEVEL,
    SOURCE_COLLEAGUE_UUID,
    SOURCE_COLLEAGUE_PROFILE,

    PROFILE_ATTRIBUTE_NAME,
    TIMELINE_POINT,
    TIMELINE_POINT_UUID,

    TIP_UUID,
    TIP,

    //notifications
    CONTACT_TEMPLATE_ID;

    /**
     * Returns corresponded parameter for string value if exist or the same parameter
     * @param parameter source parameter
     *
     * @return event for corresponded string value
     */
    public static FlowParameters getCorrespondedStringParameter(FlowParameters parameter) {
        if (START_DATE.equals(parameter)) {
            return START_DATE_S;
        } else if (BEFORE_START_DATE.equals(parameter)) {
            return BEFORE_START_DATE_S;
        } else if (END_DATE.equals(parameter)) {
            return END_DATE_S;
        } else if (BEFORE_END_DATE.equals(parameter)) {
            return BEFORE_END_DATE_S;
        }
        return parameter;
    }
}
