package com.tesco.pma.profile.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackages = "com.tesco.pma.profile")
//        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,
//                classes = {RestController.class, Service.class}))
//@MapperScan("com.tesco.pma.profile.dao")
public class ProfileModuleConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileModuleConfiguration.class);

    @PostConstruct
    public void postConstruct() {
        LOGGER.info("Profile module loaded !");
    }


}
