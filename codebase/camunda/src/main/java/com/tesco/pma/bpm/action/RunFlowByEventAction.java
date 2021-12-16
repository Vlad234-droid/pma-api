package com.tesco.pma.bpm.action;

import java.util.HashMap;
import java.util.Map;

import com.tesco.pma.logging.TraceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventResponse;
import com.tesco.pma.event.EventResponseSupport;
import com.tesco.pma.event.controller.Action;
import com.tesco.pma.event.controller.EventException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.spring.tx.ThreadTransactionHolder;

import lombok.extern.slf4j.Slf4j;

import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;

@Slf4j
@Component
public class RunFlowByEventAction implements Action {

    @Autowired
    private ProcessManagerService processManagerService;

    @Override
    public EventResponse perform(Event event) throws EventException {
        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put(ExecutionContext.Params.EC_EVENT.name(), event);
            ctx.put(TRACE_ID_HEADER, TraceUtils.getTraceId().getValue());

            Map<String, Object> holder = new HashMap<>();
            ctx.put(ExecutionContext.Params.EC_OUTPUT.name(), holder);

            String pid = processManagerService.runProcessByEvent(event.getEventName(), ctx);
            if (log.isDebugEnabled()) {
                log.debug(FlowMessages.APP_INFO.format("Process with pid='%s' has been finished successfully", pid));
            }
            return new EventResponseSupport(event.getEventName(), EventResponse.END, holder);
        } catch (Exception e) {
            try {
                ThreadTransactionHolder.rollback();
            } catch (Exception ex) {
                log.warn("Can't rollback transaction", ex);
            }
            throw new EventException(event, FlowMessages.FLOW_ERROR_RUNTIME.format("Process has been finished with error"), e);
        }
    }
}
