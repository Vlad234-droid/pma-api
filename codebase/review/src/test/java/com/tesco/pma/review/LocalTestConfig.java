package com.tesco.pma.review;

import com.tesco.pma.configuration.MessageSourceConfig;
import com.tesco.pma.error.ApiExceptionHandler;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@Import({MessageSourceAutoConfiguration.class,
        MessageSourceConfig.class,
        HttpMessageConvertersAutoConfiguration.class,
        ApiExceptionHandler.class})
public class LocalTestConfig {

}
