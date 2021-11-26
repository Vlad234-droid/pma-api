package com.tesco.pma.colleague.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class ColleagueSimple implements Serializable {
    private static final long serialVersionUID = -2579287324831609301L;

    UUID uuid;
    String firstName;
    String middleName;
    String lastName;
    String jobName;
    String businessType;
}
