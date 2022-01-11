package com.tesco.pma.colleague.profile.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypedAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    UUID colleagueUuid;

    String name;

    String value;

    AttributeType type;

}
