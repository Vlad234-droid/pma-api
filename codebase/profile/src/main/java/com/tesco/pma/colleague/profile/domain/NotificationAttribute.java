package com.tesco.pma.colleague.profile.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationAttribute {

    private UUID id;
    private String name;
    private NotificationAttributeType type;

}
