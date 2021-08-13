package com.tesco.pma.service.colleague.client.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Colleague {
    private UUID colleagueUUID;
    private Profile profile;
    private Contact contact;
    private ExternalSystems externalSystems;
}
