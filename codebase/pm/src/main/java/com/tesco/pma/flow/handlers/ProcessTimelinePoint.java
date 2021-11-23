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
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_DELAY;
import static com.tesco.pma.cycle.api.model.PMTimelinePointElement.PM_TIMELINE_POINT_START_TIME;

/**
 * Calculates all required timeline dates for fiscal year performance cycle
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:25
 */
@Slf4j
@Component
public class ProcessTimelinePoint extends CamundaAbstractFlowHandler {
    @Override
    protected void execute(ExecutionContext context) throws Exception {
        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        if (PMCycleType.FISCAL != cycle.getType()) {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect cycle type: " + cycle.getType());
        }
        //todo handle cycle statuses

        Map<String, String> props = getParentProperties(context);
        if (!props.isEmpty()) {
            var type = PMElementType.getByCode(props.get(PMElement.PM_TYPE));
            if (PMElementType.TIMELINE_POINT == type) {
                processTimelinePoint(context, cycle, props);
            } else if (PMElementType.REVIEW == type) {
                processReview(context, cycle, props);
            }
        } else {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect configuration: none required parameters are specified");
        }
    }

    Map<String, String> getParentProperties(ExecutionContext context) {
        var delegate = ((CamundaExecutionContext) context).getDelegateExecution();
        var parent = (BaseElement) delegate.getBpmnModelElementInstance().getParentElement();
        return PMProcessModelParser.getExternalProperties(parent);
    }

    void processTimelinePoint(ExecutionContext context, PMCycle cycle, Map<String, String> props) throws ProcessExecutionException {
        if (props.containsKey(PM_TIMELINE_POINT_START_DELAY)) {
            //todo logic
            var startDate = cycle.getStartTime();
            context.setVariable(FlowParameters.START_DATE, startDate);
        } else if (props.containsKey(PM_TIMELINE_POINT_START_TIME)) {
            //todo logic
            var startDate = cycle.getStartTime();
            context.setVariable(FlowParameters.START_DATE, startDate);
        } else {
            //todo replace by required exception
            throw new ProcessExecutionException("Incorrect configuration: none required parameters are specified: "
                    + PM_TIMELINE_POINT_START_DELAY + ',' + PM_TIMELINE_POINT_START_TIME);
        }
    }

    void processReview(ExecutionContext context, PMCycle cycle, Map<String, String> props) {
        //todo logic
        var startDate = cycle.getStartTime();
        context.setVariable(FlowParameters.START_DATE, startDate);
    }
}
