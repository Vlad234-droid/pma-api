package com.tesco.pma.reporting.dashboard;

import com.tesco.pma.configuration.MessageSourceConfig;
import com.tesco.pma.configuration.MvcRequestParamBeanConfiguration;
import com.tesco.pma.error.ApiExceptionHandler;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@Import({MessageSourceAutoConfiguration.class,
        MessageSourceConfig.class,
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        RestTemplateAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        ApiExceptionHandler.class,
        MvcRequestParamBeanConfiguration.class
})
public class LocalTestConfig {

}