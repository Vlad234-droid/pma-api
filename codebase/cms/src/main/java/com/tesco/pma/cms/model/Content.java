package com.tesco.pma.cms.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class Content {

    private UUID id;
    private String key;
    private Integer version;
    private ContentStatus status;
    private String title;
    private String description;
    private String imageLink;
    private UUID createdBy;
    private Instant createdTime;

}
