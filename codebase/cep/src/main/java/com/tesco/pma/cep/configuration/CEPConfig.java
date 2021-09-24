package com.tesco.pma.cep.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.tesco.pma.cep")
public class CEPConfig {

    /**
     * Creates bean with CEP feeds
     *
     * @return set of CEP feeds
     */
    @Bean
    @ConfigurationProperties(prefix = "tesco.application.external-endpoints.cep")
    public CEPFeedsProperties cepFeedsProperties() {
        return new CEPFeedsProperties();
    }

}


