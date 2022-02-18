package com.tesco.pma.cep.v2.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "tesco.application.external-endpoints.cep.subscribe")
@Data
class CEPSubscribeProperties {
    private Map<String, String> feeds;
}
