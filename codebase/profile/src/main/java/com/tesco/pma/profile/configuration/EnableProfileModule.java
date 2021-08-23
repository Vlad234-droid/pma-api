package com.tesco.pma.profile.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(ProfileModuleConfiguration.class)
@Configuration
public @interface EnableProfileModule {
}
