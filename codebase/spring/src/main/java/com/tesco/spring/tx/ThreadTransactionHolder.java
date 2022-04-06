package com.tesco.spring.tx;

import java.util.Optional;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * Solution to use transaction manually since there is an issue with transactions in Spring.
 * Should be replaced once it will be fixed
 * Link DB transaction to thread.
 */
@Slf4j
public final class ThreadTransactionHolder { // NOPMD

    private static ThreadLocal<TransactionInfo> transactionInfoHolder = new ThreadLocal<>();

    private ThreadTransactionHolder() {
        throw new IllegalStateException("Can't be instantiated");
    }

    public static TransactionStatus startTransaction(PlatformTransactionManager transactionManager,
                                                     DefaultTransactionDefinition txDefinition) {
        if (transactionManager == null) {
            throw new IllegalArgumentException("transactionManager must be specified");
        }
        final var transactionInfo = transactionInfoHolder.get();
        if (transactionInfo != null) {
            throw new IllegalStateException("ThreadTransactionHolder supports only single transaction per thread. Transaction "
                    + transactionInfo + " should be finished before usage.");
        }

        final var transactionStatus = transactionManager.getTransaction(txDefinition);
        transactionInfoHolder.set(new TransactionInfo(transactionManager, transactionStatus, txDefinition));
        log.debug("Start transaction: {}({}), tx: {}", txDefinition.getName(), txDefinition, transactionStatus);
        return transactionStatus;
    }

    public static void commit() {
        final var transactionInfo = transactionInfoHolder.get();
        if (transactionInfo != null) {
            final var transactionStatus = transactionInfo.getTransactionStatus();
            final var txDefinition = transactionInfo.getTransactionDefinition();
            try {
                try {
                    transactionInfo.getTransactionManager().commit(transactionStatus);
                    log.debug("Commit transaction: {}({})", txDefinition.getName(), txDefinition);
                } catch (Exception ex) {
                    log.debug("Can't commit transaction: {}({})", txDefinition.getName(), txDefinition, ex);
                    throw ex;
                }
            } finally {
                transactionInfoHolder.remove();
            }
        } else {
            log.warn("Can't perform commit operation due to absent transaction.");
        }
    }

    public static void rollback() {
        final var transactionInfo = transactionInfoHolder.get();
        if (transactionInfo != null) {
            final var transactionStatus = transactionInfo.getTransactionStatus();
            final var txDefinition = transactionInfo.getTransactionDefinition();
            try {
                try {
                    transactionInfo.getTransactionManager().rollback(transactionStatus);
                    log.debug("Commit rollback: {}({})", txDefinition.getName(), txDefinition);
                } catch (Exception ex) {
                    log.debug("Can't rollback transaction: {}({})", txDefinition.getName(), txDefinition, ex);
                    throw ex;
                }
            } finally {
                transactionInfoHolder.remove();
            }
        } else {
            log.warn("Can't perform rollback operation due to absent transaction.");
        }
    }

    private static final class TransactionInfo {

        private final PlatformTransactionManager transactionManager;
        private final TransactionStatus transactionStatus;
        private final DefaultTransactionDefinition transactionDefinition;

        public TransactionInfo(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus,
                               DefaultTransactionDefinition transactionDefinition) {
            this.transactionManager = Optional.ofNullable(transactionManager)
                    .orElseThrow(() -> new IllegalArgumentException("transactionManager must be specified"));
            this.transactionStatus = Optional.ofNullable(transactionStatus)
                    .orElseThrow(() -> new IllegalArgumentException("transactionStatus must be specified"));
            this.transactionDefinition = transactionDefinition;
        }

        public PlatformTransactionManager getTransactionManager() {
            return transactionManager;
        }

        public TransactionStatus getTransactionStatus() {
            return transactionStatus;
        }

        public DefaultTransactionDefinition getTransactionDefinition() {
            return transactionDefinition;
        }

        @Override
        public String toString() {
            return "TransactionInfo {transactionDefinition.name=" + transactionDefinition.getName() + "}";
        }
    }
}
