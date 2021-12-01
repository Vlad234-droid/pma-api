package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.model.PMProcessModelParser;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_BEFORE_END;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_BEFORE_START;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_DURATION;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_START;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_START_DELAY;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_DELAY;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_TIME;
import static com.tesco.pma.flow.handlers.ProcessTimelinePoint.PropertyNames.BEFORE_END;
import static com.tesco.pma.flow.handlers.ProcessTimelinePoint.PropertyNames.BEFORE_START;
import static com.tesco.pma.flow.handlers.ProcessTimelinePoint.PropertyNames.DURATION;
import static com.tesco.pma.flow.handlers.ProcessTimelinePoint.PropertyNames.START;
import static com.tesco.pma.flow.handlers.ProcessTimelinePoint.PropertyNames.START_DELAY;

/**
 * Calculates all required timeline point's dates for a fiscal year performance cycle
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:25
 */
@Slf4j
@Component
public class ProcessTimelinePoint extends CamundaAbstractFlowHandler {

    private final DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    enum PropertyNames {
        START,
        START_DELAY,
        BEFORE_START,
        BEFORE_END,
        DURATION
    }

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        if (PMCycleType.FISCAL != cycle.getType()) {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect cycle type: " + cycle.getType());
        }
        //todo handle cycle statuses

        var parent = getParent(context);
        if (PMElementType.TIMELINE_POINT == parent.getType()) {
            processTimelinePoint(context, cycle, parent);
        } else if (PMElementType.REVIEW == parent.getType()) {
            processReview(context, cycle, parent);
        } else {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect configuration: none required parameters are specified");
        }
    }

    PMElement getParent(ExecutionContext context) {
        var delegate = ((CamundaExecutionContext) context).getDelegateExecution();
        var activity = (Activity) delegate.getBpmnModelElementInstance().getParentElement();
        return PMProcessModelParser.fillPMElement(activity, new PMElement());
    }

    void processTimelinePoint(ExecutionContext context, PMCycle cycle, PMElement element) throws ProcessExecutionException {
        processTimes(context, cycle, element.getProperties(), Map.of(
                START, PM_TIMELINE_POINT_START_TIME,
                START_DELAY, PM_TIMELINE_POINT_START_DELAY
        ));
    }

    void processReview(ExecutionContext context, PMCycle cycle, PMElement element) throws ProcessExecutionException {
        processTimes(context, cycle, element.getProperties(), Map.of(
                START, PM_REVIEW_START,
                START_DELAY, PM_REVIEW_START_DELAY,
                BEFORE_START, PM_REVIEW_BEFORE_START,
                BEFORE_END, PM_REVIEW_BEFORE_END,
                DURATION, PM_REVIEW_DURATION
        ));
    }

    private void processTimes(ExecutionContext context, PMCycle cycle, Map<String, String> props, Map<PropertyNames, String> names)
            throws ProcessExecutionException {
        var startDate = processStartDate(context, props, names, cycle.getStartTime());
        var endDate = processEndDate(context, props, names, startDate);
        processBeforeStart(context, props, names, startDate);
        processBeforeEnd(context, props, names, startDate, endDate);
    }

    private void processBeforeEnd(ExecutionContext context, Map<String, String> props, Map<PropertyNames, String> names,
                                  LocalDate startDate, LocalDate endDate) throws ProcessExecutionException {
        if (isPresent(props, names, BEFORE_END)) {
            try {
                var period = parsePeriod(props, names, BEFORE_END);
                var before = endDate.minus(period);
                setDateVariable(context, FlowParameters.BEFORE_END_DATE, before.isBefore(startDate) ? startDate : before);
            } catch (DateTimeParseException e) {
                throw incorrectParameter(props, names, BEFORE_END, e);
            }
        }
    }

    private LocalDate processEndDate(ExecutionContext context, Map<String, String> props, Map<PropertyNames, String> names,
                                   LocalDate startDate) throws ProcessExecutionException {
        var endDate = startDate;
        if (isPresent(props, names, DURATION)) {
            try {
                var period = parsePeriod(props, names, DURATION);
                endDate = startDate.plus(period);
                setDateVariable(context, FlowParameters.END_DATE, endDate);
            } catch (DateTimeParseException e) {
                throw incorrectParameter(props, names, DURATION, e);
            }
        }
        return endDate;
    }

    private void processBeforeStart(ExecutionContext context, Map<String, String> props, Map<PropertyNames, String> names,
                                    LocalDate startTime) throws ProcessExecutionException {
        if (isPresent(props, names, BEFORE_START)) {
            try {
                var period = parsePeriod(props, names, BEFORE_START);
                var before = startTime.minus(period);
                setDateVariable(context, FlowParameters.BEFORE_START_DATE, before);
            } catch (DateTimeParseException e) {
                throw incorrectParameter(props, names, BEFORE_START, e);
            }
        }
    }

    private LocalDate processStartDate(ExecutionContext context, Map<String, String> props, Map<PropertyNames, String> names,
                                     Instant cycleStartTime) throws ProcessExecutionException {
        var startTime = LocalDate.ofInstant(cycleStartTime, ZoneOffset.UTC);
        if (isPresent(props, names, START_DELAY)) {
            try {
                var period = parsePeriod(props, names, START_DELAY);
                startTime = startTime.plus(period);
            } catch (DateTimeParseException e) {
                throw incorrectParameter(props, names, START_DELAY, e);
            }
        } else if (isPresent(props, names, START)) {
            try {
                startTime = parseLocalDate(props, names, START);
            } catch (DateTimeParseException e) {
                throw incorrectParameter(props, names, START, e);
            }
        } else {
            log.debug("No parameter specified: " + names.get(START_DELAY) + ", " + names.get(START)
                    + "\nCycle start date is used: " + formatDate(startTime));
        }
        setDateVariable(context, FlowParameters.START_DATE, startTime);
        return startTime;
    }

    private void setDateVariable(ExecutionContext context, FlowParameters variable, LocalDate date) {
        context.setVariable(variable, formatDate(date));
    }

    private String get(Map<String, String> props, Map<PropertyNames, String> names, PropertyNames name) {
        return props.get(names.get(name));
    }

    private boolean isPresent(Map<String, String> props, Map<PropertyNames, String> names, PropertyNames name) {
        return props.containsKey(names.get(name));
    }

    private Period parsePeriod(Map<String, String> props, Map<PropertyNames, String> names, PropertyNames name) {
        return Period.parse(get(props, names, name));
    }

    private LocalDate parseLocalDate(Map<String, String> props, Map<PropertyNames, String> names, PropertyNames name) {
        return dtFormatter.parse(get(props, names, name), LocalDate::from);
    }

    private String formatDate(LocalDate dateTime) {
        return dtFormatter.format(dateTime);
    }

    //todo replace by required exception
    private ProcessExecutionException incorrectParameter(Map<String, String> props, Map<PropertyNames, String> names, PropertyNames name,
                                                         DateTimeParseException exception) {
        log.error("Incorrect configuration: " + names.get(name) + ":" + props.get(names.get(name)), exception);
        return new ProcessExecutionException("Incorrect configuration: "
                + names.get(name) + ":" + props.get(names.get(name)));
    }
}
