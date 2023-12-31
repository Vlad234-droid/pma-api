package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMProcessModelParser;
import com.tesco.pma.flow.FlowParameters;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.instance.Activity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    public static PMElement getParentModelElement(ExecutionContext context) {
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
            if (eventProperties.containsKey(FlowParameters.COLLEAGUE_UUID.name())) {
                return UUID.fromString(eventProperties.get(FlowParameters.COLLEAGUE_UUID.name()).toString());
            }
        }
        return null;
    }

    /**
     * Gets a event property
     *
     * @param context  execution context
     * @param property property name
     * @param cls      type
     * @return value
     */
    public static <T> T getEventProperty(ExecutionContext context, String property, Class<T> cls) {
        var event = context.getEvent();
        if (event != null) {
            var eventProperties = event.getEventProperties();
            if (eventProperties.containsKey(property)) {
                return cls.cast(eventProperties.get(property));
            }
        }
        return null;
    }

    /**
     * Returns instant at start of date in UTC time zone
     *
     * @param date source date
     * @return instant
     */
    public static Instant dateToInstant(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    /**
     * Converts instant to date in UTC
     *
     * @param datetime source instant
     * @return date
     */
    public static LocalDate instantToDate(Instant datetime) {
        return LocalDate.ofInstant(datetime, ZoneOffset.UTC);
    }
}
