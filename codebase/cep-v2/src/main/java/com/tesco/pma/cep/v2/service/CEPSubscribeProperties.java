package com.tesco.pma.cep.v2.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tesco.application.external-endpoints.cep.subscribe")
@Data
class CEPSubscribeProperties {
    private boolean force;
}
