package com.tesco.pma.service.colleague.client.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Colleague {
    UUID colleagueUUID;
    Profile profile;
    Contact contact;
}
