package com.tesco.pma.profile.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileAttribute {

    UUID colleagueUuid;

    String name;

    String title;

    String value;

    AttributeType type;

}
