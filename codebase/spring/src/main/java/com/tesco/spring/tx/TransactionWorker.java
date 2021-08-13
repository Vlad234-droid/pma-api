package com.tesco.spring.tx;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * The interface implemented to perform the work within a transaction.
 */
public interface TransactionWorker {

    /**
     * Perform the work in a new transaction and return result.
     * 
     * @param txManager - concrete transaction manager
     * @param txSupplier - will be processed within a transaction
     * @return return result
     * @throws Exception
     */
    <T> T doWork(PlatformTransactionManager txManager, TxSupplier<T> txSupplier) throws Exception;

    /**
     * Perform the work in a new transaction without returning result
     * 
     * @param txManager - concrete transaction manager
     * @param txExecutor - will be processed within a transaction
     * @throws Exception
     */
    default void doWorkWithoutResult(PlatformTransactionManager txManager, TxExecutor txExecutor) throws Exception {
        doWork(txManager, () -> {
            txExecutor.execute();
            return null;
        });
    }

    @FunctionalInterface
    interface TxSupplier<T> {

        T get() throws Exception;
    }

    @FunctionalInterface
    interface TxExecutor {

        void execute() throws Exception;
    }
}
