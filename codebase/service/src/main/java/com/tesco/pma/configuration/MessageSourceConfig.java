package com.tesco.pma.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public NamedMessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new NamedMessageSourceAccessor(messageSource, Locale.ENGLISH);
    }
}
