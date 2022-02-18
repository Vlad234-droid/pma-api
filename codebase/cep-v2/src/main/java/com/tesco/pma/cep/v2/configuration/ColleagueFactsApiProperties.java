package com.tesco.pma.cep.v2.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tesco.application.external-endpoints.colleague-api.force")
@Data
public class ColleagueFactsApiProperties {
    private boolean force;
}

