package com.tesco.pma.dao.config;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 28.01.2019 Time: 15:28
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties
public class MybatisDefaultConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.default")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager defaultTransactionManager(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return new DataSourceTransactionManager(defaultDataSource);
    }

    @ConfigurationProperties(prefix = "spring.datasource.default.mybatis-session")
    @Bean
    @Primary
    public MybatisProperties defaultMybatisSessionProperties() {
        return new MybatisProperties();
    }

    @Bean
    public BiFunction<MybatisProperties, DataSource, SqlSessionFactory> sqlSessionFactoryCreator(
            ObjectProvider<Interceptor[]> interceptorsProvider,
            ObjectProvider<TypeHandler[]> typeHandlersProvider,
            ObjectProvider<LanguageDriver[]> languageDriversProvider,
            ResourceLoader resourceLoader,
            ObjectProvider<DatabaseIdProvider> databaseIdProvider,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {

        return (mybatisProperties, ds) -> {
            try {
                return new MybatisAutoConfiguration(mybatisProperties, interceptorsProvider, typeHandlersProvider,
                        languageDriversProvider, resourceLoader, databaseIdProvider,
                        configurationCustomizersProvider).sqlSessionFactory(ds);
            } catch (Exception e) {
                throw new BeanCreationException("Exception on creating SqlSessionFactory bean", e);
            }
        };
    }

    @Bean
    @Primary
    public SqlSessionFactory defaultSqlSessionFactory(
            MybatisProperties defaultMybatisSessionProperties,
            @Qualifier("defaultDataSource") DataSource defaultDataSource,
            BiFunction<MybatisProperties, DataSource, SqlSessionFactory> sqlSessionFactoryCreator) {

        return sqlSessionFactoryCreator.apply(defaultMybatisSessionProperties, defaultDataSource);
    }

    @Bean
    public MapperScannerConfigurer defaultMapperScannerConfigurer(Environment environment) {
        var basePackage = environment.getProperty("spring.datasource.default.mybatis-session.configuration-properties.base-package");
        var configurer = new MapperScannerConfigurer();
        configurer.setBasePackage(basePackage);
        configurer.setSqlSessionFactoryBeanName("defaultSqlSessionFactory");
        return configurer;
    }

    @ConfigurationProperties(prefix = "spring.datasource.default.mybatis-session.configuration-properties")
    @Bean("mybatisSessionConfigurationProperties")
    public Properties defaultMybatisSessionConfigurationProperties() {
        return new Properties();
    }
}
