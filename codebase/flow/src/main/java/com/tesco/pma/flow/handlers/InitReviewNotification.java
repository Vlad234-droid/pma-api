package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMReviewType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        context.setVariable(FlowParameters.COLLEAGUE_UUID, getColleagueUUID());
        context.setVariable(FlowParameters.REVIEW_TYPE, getReviewType());
        context.setVariable(FlowParameters.REVIEW_UUID, getReviewUUID());
        context.setVariable(FlowParameters.IS_MANAGER, isManager());
    }

    public boolean isManager() {
        return false;
    }

    public PMReviewType getReviewType() {
        return null;
    }

    public UUID getReviewUUID() {
        return null;
    }

    public UUID getColleagueUUID() {
        return null;
    }
}