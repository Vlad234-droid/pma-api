package com.tesco.pma.bpm.camunda.flow.listener;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.tesco.spring.tx.ThreadTransactionHolder;

public class StartDBTransactionListenerDelegate implements ExecutionListener {

    private final PlatformTransactionManager transactionManager;

    public StartDBTransactionListenerDelegate(PlatformTransactionManager transactionManager) {
        this.transactionManager = Optional.ofNullable(transactionManager)
                .orElseThrow(() -> new IllegalArgumentException("transactionManager must be specified"));
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        final var transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionDefinition.setName(execution.getActivityInstanceId());

        ThreadTransactionHolder.startTransaction(transactionManager, transactionDefinition);
    }
}
