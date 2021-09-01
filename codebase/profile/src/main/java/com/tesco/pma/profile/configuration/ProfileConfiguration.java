package com.tesco.pma.profile.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackages = "com.tesco.pma.profile")
public class ProfileConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileConfiguration.class);

    @PostConstruct
    public void postConstruct() {
        LOGGER.info("Profile module loaded !");
    }


}
