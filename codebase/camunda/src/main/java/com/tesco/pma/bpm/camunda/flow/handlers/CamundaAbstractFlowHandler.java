package com.tesco.pma.bpm.camunda.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.spring.tx.TransactionWorker;
import com.tesco.spring.tx.TransactionWorker.TxExecutor;
import com.tesco.spring.tx.TransactionWorker.TxSupplier;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.function.Supplier;

public abstract class CamundaAbstractFlowHandler implements JavaDelegate { //NOPMD

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected abstract void execute(ExecutionContext context) throws Exception;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(FlowMessages.FLOW_HANDLER_START.name());
        }
        try {
            execute(createExecutionContext(execution));
        } catch (Exception e) {
            // we need to log exception since the one is hiding in case
            // when an exception is occurred within a transaction block
            log.error(FlowMessages.FLOW_ERROR_RUNTIME.name(), e);
            if (log.isInfoEnabled()) {
                log.info(FlowMessages.FLOW_HANDLER_END_ERROR.name());
            }
            throw e;
        }
        if (log.isInfoEnabled()) {
            log.info(FlowMessages.FLOW_HANDLER_END.name());
        }
    }

    protected ExecutionContext createExecutionContext(DelegateExecution execution) {
        return new CamundaExecutionContext(execution);
    }

    protected <T> T executeInTx(ExecutionContext context, TxSupplier<T> txSupplier) throws Exception {
        return executeInTx(() -> context.getBean(PlatformTransactionManager.class), context, txSupplier);
    }

    protected <T> T executeInTx(ExecutionContext context, String txManagerQualifier, TxSupplier<T> txSupplier) throws Exception {
        return executeInTx(() -> context.getBean(txManagerQualifier, PlatformTransactionManager.class), context, txSupplier);
    }

    private <T> T executeInTx(Supplier<PlatformTransactionManager> supplier, ExecutionContext context, TxSupplier<T> txSupplier)
            throws Exception {
        return context.getBean(TransactionWorker.class).doWork(supplier.get(), txSupplier);
    }

    protected void executeInTxWithoutResult(ExecutionContext context, TxExecutor txExecutor) throws Exception {
        executeInTxWithoutResult(() -> context.getBean(PlatformTransactionManager.class), context, txExecutor);
    }

    protected void executeInTxWithoutResult(ExecutionContext context, String txManagerQualifier, TxExecutor txExecutor) throws Exception {
        executeInTxWithoutResult(() -> context.getBean(txManagerQualifier, PlatformTransactionManager.class), context, txExecutor);
    }

    private void executeInTxWithoutResult(Supplier<PlatformTransactionManager> supplier, ExecutionContext context, TxExecutor txExecutor)
            throws Exception {
        context.getBean(TransactionWorker.class).doWorkWithoutResult(supplier.get(), txExecutor);
    }
}