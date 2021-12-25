package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InitReviewNotificationHandler extends AbstractInitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        context.setVariable(FlowParameters.REVIEW_TYPE, getReviewType(context.getEvent()));
    }

    protected PMReviewType getReviewType(Event event) {
        return (PMReviewType) event.getEventProperty(FlowParameters.REVIEW_TYPE.name());
    }


}