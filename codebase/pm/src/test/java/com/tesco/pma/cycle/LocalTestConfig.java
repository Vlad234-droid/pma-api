package com.tesco.pma.cycle;

import com.tesco.pma.configuration.MessageSourceConfig;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ApiExceptionHandler;
import com.tesco.pma.util.ResourceProvider;
import com.tesco.pma.process.service.ClasspathResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@Import({MessageSourceAutoConfiguration.class,
        MessageSourceConfig.class,
        HttpMessageConvertersAutoConfiguration.class,
        ApiExceptionHandler.class,
        JacksonAutoConfiguration.class})
public class LocalTestConfig {

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Bean
    public ResourceProvider classpathResourceProvider() {
        return new ClasspathResourceProvider(messageSourceAccessor);
    }

}
