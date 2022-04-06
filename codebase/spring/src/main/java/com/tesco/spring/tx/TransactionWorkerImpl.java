package com.tesco.spring.tx;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionWorkerImpl implements TransactionWorker {

    @Override
    public <T> T doWork(PlatformTransactionManager txManager, TxSupplier<T> txSupplier) throws Exception {
        Objects.requireNonNull(txManager, "PlatformTransactionManager must be specified");
        Objects.requireNonNull(txSupplier, "txSupplier must be specified");

        final var transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionDefinition.setName(UUID.randomUUID().toString());

        log.debug("Start new transaction:{}", transactionDefinition);
        final var status = txManager.getTransaction(transactionDefinition);
        try {
            final T result = txSupplier.get();
            log.debug("Commit transaction:{}", transactionDefinition);
            txManager.commit(status);
            return result;
        } catch (Exception ex) {
            log.warn("Rollback transaction:{}", transactionDefinition, ex);
            txManager.rollback(status);
            throw ex;
        }
    }
}
