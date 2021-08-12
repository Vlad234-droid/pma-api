package com.tesco.pma.bpm.camunda.flow;

import static org.mockito.ArgumentMatchers.any;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.tesco.pma.bpm.camunda.starter.CamundaConfig;

/**
 * This config can be used to test camunda flow.
 * Usage:
 * @SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
 * Required resources you can find in <a href="file:/resources/application-test.yml">/resources/application-test.yml</a>
 */
@Configuration
@Profile("test")
@Import({CamundaConfig.class, CamundaBpmAutoConfiguration.class})
public class CamundaSpringBootTestConfig {

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
        Mockito.when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        return transactionManager;
    }
}