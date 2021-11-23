package com.tesco.pma.colleague.config.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class DefaultAttribute {

    private UUID id;
    private String name;
    private String value;
    private DefaultAttributeCriteria criteria;
    private DefaultAttributeCategory category;

}
