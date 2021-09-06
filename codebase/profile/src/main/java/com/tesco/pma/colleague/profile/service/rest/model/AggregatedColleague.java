package com.tesco.pma.colleague.profile.service.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.Colleague;
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
public class AggregatedColleague {

    Colleague colleague;

    Colleague lineManager;

    List<ProfileAttribute> profileAttributes;

}
