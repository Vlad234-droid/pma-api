package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.logging.LogFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_COLLEAGUE_UUID_ABSENT;
import static com.tesco.pma.flow.exception.ErrorCodes.EVENT_INVALID_COLLEAGUE_UUID_FORMAT;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMDeleteColleagueHandler extends CamundaAbstractFlowHandler {

    private final PMColleagueCycleService pmColleagueCycleService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) {
        try {
            Optional.ofNullable(HandlerUtils.getEventColleagueUuid(context))
                    .ifPresentOrElse(
                            uuid -> pmColleagueCycleService.changeStatusForColleague(uuid, PMCycleStatus.ACTIVE, PMCycleStatus.INACTIVE),
                            () -> log.warn(LogFormatter.formatMessage(messageSourceAccessor, EVENT_COLLEAGUE_UUID_ABSENT))
                    );
        } catch (IllegalArgumentException e) {
            log.warn(LogFormatter.formatMessage(messageSourceAccessor, EVENT_INVALID_COLLEAGUE_UUID_FORMAT));
        }
    }
}
