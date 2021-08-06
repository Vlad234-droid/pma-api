package com.tesco.pma;

import com.tesco.pma.dao.config.MybatisDefaultConfig;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.12.2020 Time: 23:05
 */
@Profile("test")
@Configuration
@ComponentScan(basePackages = "com.tesco")
@Import({MessageSourceAutoConfiguration.class,
        MybatisDefaultConfig.class,
        JacksonAutoConfiguration.class,
        // for security
        HttpMessageConvertersAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        RestTemplateAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
})
public class TestConfig {

    @Bean
    public BuildProperties buildProperties() {
        return new BuildProperties(new Properties());
    }

    @Bean
    public ApplicationAvailability applicationAvailability() {
        return new ApplicationAvailabilityBean();
    }
}
