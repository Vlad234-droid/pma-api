package com.tesco.pma.configuration;

import lombok.Data;

@Data
public class HealthIndicatorProperties {
    protected String url;
    protected String name;
    protected String description;
    protected String beanName;
    protected String reference;
    protected String validationQuery;
}