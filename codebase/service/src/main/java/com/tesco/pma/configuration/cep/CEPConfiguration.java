package com.tesco.pma.configuration.cep;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CEPConfiguration {

    /**
     * Creates bean with CEP properties
     *
     * @return set of CEP properties
     */
    @Bean
    @ConfigurationProperties(prefix = "tesco.application.external-endpoints.cep")
    public CEPProperties cepProperties() {
        return new CEPProperties();
    }
}
