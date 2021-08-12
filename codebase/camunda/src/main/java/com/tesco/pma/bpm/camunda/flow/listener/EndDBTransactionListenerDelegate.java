package com.tesco.pma.bpm.camunda.flow.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import com.tesco.spring.tx.ThreadTransactionHolder;

/**
 *
 */
public class EndDBTransactionListenerDelegate implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (execution instanceof ActivityExecution) {
            if (((ActivityExecution) execution).isEnded()) {
                ThreadTransactionHolder.rollback();
            } else {
                ThreadTransactionHolder.commit();
            }
        } else {
            throw new IllegalStateException("execution must be 'ActivityExecution' type");
        }
    }
}
