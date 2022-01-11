package com.tesco.pma.bpm.camunda.flow;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.event.Event;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public abstract class FlowTestUtil { //NOPMD

    private FlowTestUtil() {
    }

    /**
     * Helps to mock execution in handler. The handler must be mocked previously.
     *
     * @param handler
     * @param consumer
     * @throws Exception
     */
    public static void mockExecutionInHandler(JavaDelegate handler, Consumer<DelegateExecution> consumer) throws Exception {
        doAnswer(invocation -> {
            consumer.accept(invocation.getArgument(0));
            return null;
        }).when(handler).execute(any());
    }

    /**
     * Factory method that creates DelegateExecutionMockBuilder. Typical usage:
     * <pre>
     * ExecutionContext execution = executionBuilder()
     *          .withVariable(ImageFlowVariables.TRANSACTION_INFORMATION, transactionInformation)
     *          .withVariable(ImageFlowVariables.IMAGE_CONFIG, imageConfig)
     *          .withVariable(ImageFlowVariables.TEMP_DIR, temp.getRoot().getAbsolutePath())
     *          .build();
     * </pre>
     *
     * @return DelegateExecutionMockBuilder instance
     */
    public static ExecutionMockBuilder executionBuilder() {
        return new ExecutionMockBuilder();
    }

    /**
     * Factory method that creates DelegateExecutionAssertion. Typical usage:
     * <pre>
     * assertThat(execution)
     *          .containsVariable(ImageFlowVariables.PAYLOAD_ITEM, payloadItem)
     *          .containsVariable(ImageFlowVariables.TRANSACTION_INFORMATION, transactionInformation)
     *          .doNotContainsVariable(ImageFlowVariables.IMAGE_BODY)
     *          .doNotContainsVariable(ImageFlowVariables.ACTUAL_ARTEFACT_TYPE);
     * </pre>
     *
     * @param executionContext ExecutionContext.
     * @return ExecutionAssertion instance
     */
    public static ExecutionAssertion assertThat(ExecutionContext executionContext) {
        return new ExecutionAssertion(executionContext);
    }

    public static class ExecutionMockBuilder {

        ExecutionContext execution;

        protected ExecutionMockBuilder() {
            execution = new TestExecutionContext();
        }

        public <E extends Enum<E>> ExecutionMockBuilder withVariable(E variable, Object value) {
            execution.setVariable(variable, value);
            return this;
        }

        public ExecutionMockBuilder withEvent(Event event) {
            return withVariable(ExecutionContext.Params.EC_EVENT, event);
        }

        public ExecutionContext build() {
            return execution;
        }

    }

    public static class ExecutionAssertion {

        ExecutionContext executionContext;

        public ExecutionAssertion(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        public <E extends Enum<E>> ExecutionAssertion containsVariable(E variable, Object value) {
            assertEquals(value, executionContext.getVariable(variable), "Execution variable " + variable + " should be set");
            return this;
        }

        public <E extends Enum<E>> ExecutionAssertion doNotContainsVariable(E variable) {
            Object value = executionContext.getNullableVariable(variable);
            assertNull(value,"Execution variable " + variable + " should not be set. Current value: " + value);
            return this;
        }

    }

    private static class TestExecutionContext extends CamundaExecutionContext {

        private static final long serialVersionUID = -1717881741280531680L;

        Map<String, Object> context = new HashMap<>();

        public TestExecutionContext() {
            super(null);
        }

        @Override
        protected Object getRawVariable(String name) {
            return context.get(name);
        }

        @Override
        protected void setRawVariable(String name, Object value) {
            context.put(name, value);
        }
    }
}
