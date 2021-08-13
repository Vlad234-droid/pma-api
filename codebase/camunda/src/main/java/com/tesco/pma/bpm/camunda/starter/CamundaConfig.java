package com.tesco.pma.bpm.camunda.starter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.camunda.bpm.spring.boot.starter.configuration.CamundaDatasourceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.bpm.camunda.CamundaProcessManagerServiceBean;
import com.tesco.pma.bpm.camunda.util.ApplicationContextProvider;
import com.tesco.spring.tx.TransactionConfig;

/**
 *
 */
@Configuration("camunda-config")
@Import(TransactionConfig.class)
public class CamundaConfig {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.camunda")
    public DataSource camundaBpmDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager camundaBpmTransactionManager(@Qualifier("camundaBpmDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public CamundaDatasourceConfiguration camundaDatasourceConfiguration() {
        return new CamundaDataSourceConfigurationImpl();
    }

    @Bean
    public ProcessManagerService processManagerService() {
        return new CamundaProcessManagerServiceBean();
    }

    @PostConstruct
    public void initializeApplicationContext() {
        ApplicationContextProvider.initialize(applicationContext);
    }
}
