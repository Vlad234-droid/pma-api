package com.tesco.pma.colleague.profile.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypedAttribute {

    UUID colleagueUuid;

    String name;

    String value;

    AttributeType type;

}
