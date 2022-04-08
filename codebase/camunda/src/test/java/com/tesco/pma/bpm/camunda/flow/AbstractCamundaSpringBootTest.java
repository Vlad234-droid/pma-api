package com.tesco.pma.bpm.camunda.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;

import com.tesco.pma.event.Event;
import com.tesco.pma.bpm.api.flow.ExecutionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractCamundaSpringBootTest {

    @Autowired
    protected ProcessEngine processEngine;

    public String runProcessByEvent(Event event) {
        Map<String, Object> ctx = createCtx(event);
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByMessage(event.getEventName(), ctx);
        return processInstance.getProcessInstanceId();
    }

    public String runProcessByEvent(Event event, Map<String, Object> ctx) {
        ctx.put(ExecutionContext.Params.EC_EVENT.name(), event);
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByMessage(event.getEventName(), ctx);
        return processInstance.getProcessInstanceId();
    }

    public String runProcess(String processName) {
        return runProcess(processName, null, null);
    }

    public String runProcess(String processName, Map<String, Object> varMap) {
        return runProcess(processName, null, varMap);
    }

    public String runProcess(String processName, String version) {
        return runProcess(processName, version, null);
    }

    public String runProcess(String processName, String version, Map<String, Object> varMap) {
        final ProcessDefinitionQuery processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionKey(processName);
        Optional.ofNullable(version).ifPresent(v -> {
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(v));
        });
        final ProcessDefinition processDefinition = processDefinitionQuery.singleResult();
        if (processDefinition == null) {
            String message = "Couldn't find process by key %s, version %s";
            throw new IllegalStateException(String.format(message, processName, version));
        }
        final RuntimeService runtimeService = processEngine.getRuntimeService();
        String instanceId;
        if (varMap != null) {
            instanceId = runtimeService.startProcessInstanceById(processDefinition.getId(), varMap).getId();
        } else {
            instanceId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
        }
        return instanceId;
    }

    public ProcessExecutionAssertion assertThatForProcess(String pid) {
        return new CamundaAssertion(processEngine.getHistoryService(), pid);
    }

    public interface ProcessExecutionAssertion {

        ProcessExecutionAssertion endIsReached();

        ExecutionAssertion activity(String activityId);
    }

    public interface ExecutionAssertion {

        ProcessExecutionAssertion neverExecuted();

        ProcessExecutionAssertion executedOnce();

        ProcessExecutionAssertion executedTwice();

        ProcessExecutionAssertion executedTimes(int times);
    }

    public static class CamundaAssertion implements ProcessExecutionAssertion, ExecutionAssertion {

        private final String pid;
        private final HistoryService historyService;

        private String activityId;

        public CamundaAssertion(HistoryService historyService, String pid) {
            this.historyService = historyService;
            this.pid = pid;
        }

        @Override
        public ProcessExecutionAssertion endIsReached() {
            HistoricProcessInstance historicProcessInstance =
                    historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(pid).singleResult();
            assertNotNull(historicProcessInstance, "Process with pid " + pid + " is not finished");
            return this;
        }

        @Override
        public ExecutionAssertion activity(String activityId) {
            this.activityId = activityId;
            return this;
        }

        @Override
        public ProcessExecutionAssertion neverExecuted() {
            return executedTimes(0);
        }

        @Override
        public ProcessExecutionAssertion executedOnce() {
            return executedTimes(1);
        }

        @Override
        public ProcessExecutionAssertion executedTwice() {
            return executedTimes(2);
        }

        @Override
        public ProcessExecutionAssertion executedTimes(int times) {
            List<HistoricActivityInstance> activityList =
                    historyService.createHistoricActivityInstanceQuery().activityId(activityId).processInstanceId(pid).list();
            int timesExecuted = activityList.size();
            assertEquals(times, timesExecuted,
                    "Activity '" + activityId + "' should be executed " + times + " time(s), " + "but is executed " + timesExecuted
                             + " time(s)");
            return this;
        }
    }

    private Map<String, Object> createCtx(Event event) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(ExecutionContext.Params.EC_EVENT.name(), event);
        return ctx;
    }

}
