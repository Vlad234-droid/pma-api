package com.tesco.pma.cms.model;

import com.tesco.pma.api.MapJson;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ContentEntry {

    private UUID uuid;
    private String key;
    private Integer version;
    private ContentStatus status;
    private String title;
    private String description;
    private UUID createdBy;
    private Instant createdTime;
    private MapJson properties;

}
