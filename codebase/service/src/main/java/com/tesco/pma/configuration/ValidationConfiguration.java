package com.tesco.pma.configuration;

import com.tesco.pma.validation.AppLocalValidatorFactoryBean;
import com.tesco.pma.validation.JacksonPropertyNodeNameProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class ValidationConfiguration { //NOPMD

    /**
     * Configure NoOp validator in MVC.
     * Method validation will be used.
     *
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer mvcValidatorNoOpConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public Validator getValidator() {
                return new Validator() {

                    @Override
                    public boolean supports(Class<?> clazz) {
                        return false;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                        // NoOp is supposed
                    }
                };
            }
        };
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean appValidator(ValidationProperties validationProperties,
                                                  PropertyNodeNameProvider propertyNodeNameProvider) {
        try (var validator = new AppLocalValidatorFactoryBean()) {

            validator.setPropertyNodeNameProvider(propertyNodeNameProvider);
            validator.setMappingLocations(validationProperties.getMappingLocations());
            validator.setValidationPropertyMap(validationProperties.getProperties());
            validator.setValidationMessageSource(messageSource(validationProperties.getMessages()));

            return validator;
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "validation")
    public ValidationProperties validationProperties() {
        return new ValidationProperties();
    }

    @Bean
    public PropertyNodeNameProvider propertyNodeNameProvider(@Autowired(required = false) ObjectMapper objectMapper) {
        if (Objects.nonNull(objectMapper)) {
            return new JacksonPropertyNodeNameProvider(objectMapper);
        }
        return new DefaultPropertyNodeNameProvider();
    }


    private MessageSource messageSource(MessageSourceProperties properties) {
        var messageSource = new ResourceBundleMessageSource();
        if (StringUtils.hasText(properties.getBasename())) {
            messageSource.setBasenames(StringUtils
                    .commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
        }
        if (properties.getEncoding() != null) {
            messageSource.setDefaultEncoding(properties.getEncoding().name());
        }
        messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        var cacheDuration = properties.getCacheDuration();
        if (cacheDuration != null) {
            messageSource.setCacheMillis(cacheDuration.toMillis());
        }
        messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        return messageSource;
    }
}
