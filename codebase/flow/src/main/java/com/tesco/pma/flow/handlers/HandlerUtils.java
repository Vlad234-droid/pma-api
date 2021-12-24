package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.model.PMProcessModelParser;
import com.tesco.pma.event.EventParams;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.instance.Activity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-12-24 16:55
 */
@Slf4j
@UtilityClass
public class HandlerUtils {

    /**
     * Returns the parent BPMN element of the current
     *
     * @param context execution context
     * @return parent element
     */
    public static PMElement getParent(ExecutionContext context) {
        var delegate = ((CamundaExecutionContext) context).getDelegateExecution();
        var activity = (Activity) delegate.getBpmnModelElementInstance().getParentElement();
        return PMProcessModelParser.fillPMElement(activity, new PMElement());
    }

    /**
     * Formats a date according to ISO_LOCAL_DATE format
     *
     * @param dateTime date
     * @return String
     */
    public static String formatDate(LocalDate dateTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(dateTime);
    }

    /**
     * Gets a colleague UUID from the event
     *
     * @param context execution context
     * @return colleague UUID
     * @throws IllegalArgumentException if UUID has got invalid format
     */
    public static UUID getEventColleagueUuid(ExecutionContext context) {
        var event = context.getEvent();
        if (event != null) {
            var eventProperties = event.getEventProperties();
            if (eventProperties.containsKey(EventParams.COLLEAGUE_UUID.name())) {
                return UUID.fromString(eventProperties.get(EventParams.COLLEAGUE_UUID.name()).toString());
            }
        }
        return null;
    }

}
