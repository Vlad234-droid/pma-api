package com.tesco.pma.flow.configuration;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 07.10.2021 Time: 22:40
 */
@Slf4j
public class ReviewStatusListener implements ExecutionListener {
    String type;

    public ReviewStatusListener(String type) {
        this.type = type;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("[PM] type: " + type
                + "; activity: " + execution.getCurrentActivityName()
                + "; event: " + execution.getEventName()
                + "; properties: " + execution.getVariables()
        );
    }
}
