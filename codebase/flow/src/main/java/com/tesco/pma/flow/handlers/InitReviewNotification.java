package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.flow.FlowParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-19 23:23
 */
@Slf4j
@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InitReviewNotification extends CamundaAbstractFlowHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var event = context.getEvent();
        var eventName = event.getEventName();
        context.setVariable(FlowParameters.EVENT_NAME, eventName);
        // Colleague colleague;
        context.setVariable(FlowParameters.REVIEW_TYPE, getReviewType());
        context.setVariable(FlowParameters.IS_MANAGER, isManager());
    }

    public boolean isManager() {
        return false;
    }

    public PMReviewType getReviewType() {
        return null;
    }
}