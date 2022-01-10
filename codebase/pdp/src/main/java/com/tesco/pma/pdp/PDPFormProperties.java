package com.tesco.pma.pdp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "tesco.application.pdp.form.key")
@Data
public class PDPFormProperties {
    private Map<String, String> pdpProperties;
}