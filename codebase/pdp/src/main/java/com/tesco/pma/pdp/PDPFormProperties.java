package com.tesco.pma.pdp;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@Data
public class PDPFormProperties {
    @Value("${tesco.application.pdp.form.key:pdp.form}")
    private String pdpFormKey;
}