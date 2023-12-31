package com.tesco.pma.bpm.camunda.flow.listener;

import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.springframework.transaction.PlatformTransactionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom listener that add functionality to start and end transaction on 'Transaction' sub process.
 */
@Slf4j
public class TransactionBpmnParseListener implements BpmnParseListener { //NOPMD

    private final PlatformTransactionManager transactionManager;

    public TransactionBpmnParseListener(PlatformTransactionManager transactionManager) {
        this.transactionManager = Optional.ofNullable(transactionManager)
                .orElseThrow(() -> new IllegalArgumentException("transactionManager must be specified"));
    }

    @Override
    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity,
                                                  ActivityImpl nestedErrorEventActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @SuppressWarnings("deprecation")
    @Override
    public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement,
                                                      ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
        log.debug("add start/end listeners to {}", transactionElement.attribute("id"));
        activity.addListener(ExecutionListener.EVENTNAME_START, new StartDBTransactionListenerDelegate(transactionManager));
        activity.addListener(ExecutionListener.EVENTNAME_END, new EndDBTransactionListenerDelegate());
    }

    @Override
    public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting,
                                                       ActivityImpl boundaryEventActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
        //NOSONAR only part of methods is supported
    }

    @Override
    public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
        //NOSONAR only part of methods is supported
    }
}
