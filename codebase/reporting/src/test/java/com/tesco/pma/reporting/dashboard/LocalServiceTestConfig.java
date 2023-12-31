package com.tesco.pma.reporting.dashboard;

import com.tesco.pma.configuration.MessageSourceConfig;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@Import({MessageSourceAutoConfiguration.class,
         MessageSourceConfig.class})
public class LocalServiceTestConfig {

}