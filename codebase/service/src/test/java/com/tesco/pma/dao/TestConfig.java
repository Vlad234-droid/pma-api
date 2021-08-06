package com.tesco.pma.dao;

import com.tesco.pma.dao.config.MybatisDefaultConfig;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.TimeZone;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.12.2020 Time: 23:05
 */
@Profile({"test"})
@Configuration
@ComponentScan(basePackages = "com.tesco.pma.dao")
@Import({MybatisDefaultConfig.class, LiquibaseAutoConfiguration.class})
public class TestConfig {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
