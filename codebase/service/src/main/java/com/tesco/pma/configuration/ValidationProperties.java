package com.tesco.pma.configuration;

import lombok.Data;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

/**
 * Properties to configure {@link LocalValidatorFactoryBean}.
 */
@Data
public class ValidationProperties {

    /**
     * Xml constraint mapping resources.
     *
     * @see <a href="https://docs.jboss.org/hibernate/validator/6.1/reference/en-US/html_single/#_adding_mapping_streams">Adding mapping streams</a>
     */
    Resource[] mappingLocations;

    /**
     * MessageSource properties for custom validation messages.
     */
    @NestedConfigurationProperty
    MessageSourceProperties messages = new MessageSourceProperties();

    /**
     * Validation provider properties e.g. "hibernate.validator.fail_fast".
     */
    Map<String, String> properties;
}
