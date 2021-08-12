package com.tesco.pma.profile.rest.model;

import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.service.colleague.client.model.Colleague;
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
public class Profile {

    Colleague colleague;

    List<ProfileAttribute> profileAttributes;

}
