package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:10
 */
@Slf4j
@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SendNotification extends CamundaAbstractFlowHandler {
    @Override
    protected void execute(ExecutionContext context) throws Exception {
        //todo
    }
}
