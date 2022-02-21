package com.tesco.pma.cep.v2.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tesco.application.colleague-changes.force")
@Data
public class ColleagueChangesProperties {
    private boolean force;
}

