package com.tesco.pma.colleague.security.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserManagementConfiguration {

    /**
     * Creates bean with User Management properties
     *
     * @return set of User Management properties
     */
    @Bean
    @ConfigurationProperties(prefix = "tesco.application.user-management")
    public UserManagementProperties userManagementProperties() {
        return new UserManagementProperties();
    }

}
