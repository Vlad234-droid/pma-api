package com.tesco.pma.cep.configuration;

import lombok.Data;

import java.util.Map;

@Data
public class CEPFeedsProperties {
    private Map<String, String> feeds;
}
