package com.tesco.pma.bpm.camunda.flow;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.pma.bpm.camunda.util.ApplicationContextProvider;
import com.tesco.pma.event.Event;

public class CamundaExecutionContext implements ExecutionContext {

    private static final long serialVersionUID = 7215268279587439949L;
    private final DelegateExecution execution;
    protected final String processDefinitionId;

    public CamundaExecutionContext(DelegateExecution execution) {
        this.execution = execution;
        if (this.execution != null && execution.getProcessDefinitionId() != null) {
            this.processDefinitionId = execution.getProcessDefinitionId().replaceAll(":\\d+", "");
        } else {
            this.processDefinitionId = "undefined";
        }
    }

    @Override
    public Event getEvent() {
        return getNullableVariable(Params.EC_EVENT);
    }

    @Override
    public <E extends Enum<E>> void setOutputVariable(E enu, Object object) {
        Map<String, Object> holder = getVariable(Params.EC_OUTPUT);
        holder.put(enu.name(), object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Enum<E>, T> T getNullableVariable(E enu) {
        return (T) getVariable(enu.name());
    }

    protected Object getVariable(String name) {
        return execution.getVariable(name);
    }

    @Override
    public <E extends Enum<E>, T> T getVariable(E enu) {
        T value = getNullableVariable(enu);
        if (value == null) {
            throw new IllegalArgumentException(FlowMessages.FLOW_ERROR_RUNTIME.format("Variable %s value shouldn't be null", enu.name()));
        }
        return value;
    }

    protected void setVariable(String name, Object value) {
        execution.setVariable(name, value);
    }

    @Override
    public <E extends Enum<E>> void setVariable(E enu, Object object) {
        setVariable(enu.name(), object);
    }

    @Override
    public String getId() {
        return execution.getId();
    }

    public DelegateExecution getDelegateExecution() {
        return execution;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getCurrentTaskDefinitionId() {
        return execution.getCurrentActivityId();
    }

    @Override
    public <T> T getBean(String name) {
        return ApplicationContextProvider.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return ApplicationContextProvider.getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return ApplicationContextProvider.getBean(requiredType);
    }
}

