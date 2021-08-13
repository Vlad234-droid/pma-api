package com.tesco.pma.bpm.api.flow;

import java.io.Serializable;

import com.tesco.pma.event.Event;

public interface ExecutionContext extends Serializable {

    enum Params {
        EC_EVENT, EC_CONTINUE_EVENT, EC_OUTPUT
    }

    Event getEvent();

    /**
     * Returns variable from context.<br/>
     * <br/>
     * <b>Note:</b> Method checks variable for not null value. Use <code>getNullableVariable(E e)</code> if null value is possible.
     *
     * @param enu Enum with variable name
     * @return variable value
     * @throws IllegalArgumentException is variable isn't set or its value is null.
     */
    <E extends Enum<E>, T> T getVariable(E enu);

    /**
     * Returns variable from context.<br/>
     *
     * @param enu Enum with variable name
     * @return variable value
     */
    <E extends Enum<E>, T> T getNullableVariable(E enu);

    /**
     * Set variable to context.<br/>
     *
     * @param enu Enum with variable name
     */
    <E extends Enum<E>> void setVariable(E enu, Object object);

    /**
     * Set variable to process output.<br/>
     *
     * @param enu Enum with variable name
     */
    <E extends Enum<E>> void setOutputVariable(E enu, Object object);

    String getId();

    /**
     * Returns BPMN process definition ID, e.g. &lt;process id="P_JOIN_PAYLOAD_PREPARING" ...&gt;&lt;/process&gt;
     * @return process's definition identifier
     */
    String getProcessDefinitionId();

    /**
     * Returns BPMN task definition ID, e.g. &lt;serviceTask id="joinItems"
     * @return task's definition identifier
     */
    String getCurrentTaskDefinitionId();

    /**
     * Get a Spring bean by name.
     * @return name
     */
    <T> T getBean(String name);

    /**
     * Get a Spring bean by name and type.
     * @return bean instance
     */
    <T> T getBean(String name, Class<T> requiredType);

    /**
     * Get a Spring bean by type.
     * @return bean instance
     */
    <T> T getBean(Class<T> requiredType);
}
