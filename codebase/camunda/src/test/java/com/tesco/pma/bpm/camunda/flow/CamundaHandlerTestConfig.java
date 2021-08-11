package com.tesco.pma.bpm.camunda.flow;

import static org.mockito.ArgumentMatchers.any;

import javax.annotation.PostConstruct;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.tesco.pma.bpm.camunda.util.ApplicationContextProvider;
import com.tesco.spring.tx.TransactionConfig;

/**
 *
 */
@Configuration
@Profile("test")
@Import(TransactionConfig.class)
public class CamundaHandlerTestConfig {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
        Mockito.when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        return transactionManager;
    }

    @PostConstruct
    public void initializeApplicationContext() {
        ApplicationContextProvider.initialize(applicationContext);
    }
}
