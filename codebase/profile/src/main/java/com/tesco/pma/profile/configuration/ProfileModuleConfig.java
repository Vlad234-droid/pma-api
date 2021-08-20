package com.tesco.pma.profile.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@ComponentScan(basePackages = "com.tesco.pma.profile")
//        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,
//                classes = {RestController.class, Service.class}))
//@MapperScan("com.tesco.pma.profile.dao")
public class ProfileModuleConfig {

}
