package com.tesco.pma.colleague.profile.domain;

import com.tesco.pma.colleague.api.Colleague;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Profile model.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColleagueProfile {
    Colleague colleague;
    List<TypedAttribute> profileAttributes;
}
