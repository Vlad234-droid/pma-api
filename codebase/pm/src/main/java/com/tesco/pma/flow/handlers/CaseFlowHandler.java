package com.tesco.pma.flow.handlers;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.tesco.pma.bpm.api.flow.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.06.2021 Time: 17:54
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CaseFlowHandler extends AbstractFlowHandler {

    @Override
    protected void execute(ExecutionContext context) {
        log.info("Property {} value {}", Params.IS_DIRECT, context.getVariable(Params.IS_DIRECT));
        log.info("Injected value {}", getInjectedValue());
    }
}
