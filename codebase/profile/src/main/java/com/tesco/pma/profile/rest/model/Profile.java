package com.tesco.pma.profile.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile {

    Colleague colleague;

    Colleague lineManager;

    List<ProfileAttribute> profileAttributes;

}
