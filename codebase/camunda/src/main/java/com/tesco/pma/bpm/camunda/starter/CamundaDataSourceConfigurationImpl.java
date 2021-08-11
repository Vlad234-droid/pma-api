package com.tesco.pma.bpm.camunda.starter;

import javax.sql.DataSource;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaDatasourceConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.property.DatabaseProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.util.StringUtils;

import com.tesco.pma.bpm.camunda.flow.listener.TransactionBpmnParseListener;

/**
 * TransactionManager can't be customized in default camunda datasource configuration.
 * @see org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultDatasourceConfiguration#transactionManager
 *
 * You should use CamundaDataSourceConfigurationImpl to replace DefaultDatasourceConfiguration in camunda spring-boot-starter.
 *
 * @see org.camunda.bpm.spring.boot.starter.CamundaBpmConfiguration#camundaDatasourceConfiguration
 *
 * https://docs.camunda.org/manual/7.9/user-guide/spring-boot-integration/configuration/#defaultdatasourceconfiguration
 *
 * Add beans in config:
 *     &amp;Bean(value = "camundaBpmDataSource")
 *     public DataSource camundaDataSource() { ... }
 *     {@literal}Bean(value = "camundaBpmTransactionManager")
 *     public PlatformTransactionManager camundaTransactionManager() { ... }
 *     or
 *     {@literal}Bean
 *     public DataSource camundaBpmDataSource() { ... }
 *     {@literal}Bean
 *     public PlatformTransactionManager camundaBpmTransactionManager() { ... }
 *
 *     {@literal}Bean
 *     public CamundaDatasourceConfiguration camundaDatasourceConfiguration() {
 *         return new CamundaDataSourceConfigurationImpl();
 *     }
 *
 * If "camundaBpmDataSource" and "camundaBpmTransactionManager" beans are absent then default dataSource and transactionManager will be used
 *
 *
 */
public class CamundaDataSourceConfigurationImpl extends AbstractCamundaConfiguration implements CamundaDatasourceConfiguration {

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired(required = false)
    @Qualifier("camundaBpmTransactionManager")
    protected PlatformTransactionManager camundaTransactionManager;

    @Autowired
    protected DataSource dataSource;

    @Autowired(required = false)
    @Qualifier("camundaBpmDataSource")
    protected DataSource camundaDataSource;

    @Override
    public void preInit(SpringProcessEngineConfiguration configuration) {
        final DatabaseProperty database = camundaBpmProperties.getDatabase();

        configuration.setTransactionManager(camundaTransactionManager != null ? camundaTransactionManager : transactionManager);
        configuration.setDataSource(camundaDataSource != null ? camundaDataSource : dataSource);

        configuration.setDatabaseType(database.getType());
        configuration.setDatabaseSchemaUpdate(database.getSchemaUpdate());

        if (!StringUtils.isEmpty(database.getTablePrefix())) {
            configuration.setDatabaseTablePrefix(database.getTablePrefix());
        }

        if (!StringUtils.isEmpty(database.getSchemaName())) {
            configuration.setDatabaseSchema(database.getSchemaName());
        }

        configuration.setJdbcBatchProcessing(database.isJdbcBatchProcessing());
        configuration.getCustomPostBPMNParseListeners().add(new TransactionBpmnParseListener(transactionManager));
    }
}
