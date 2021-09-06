package com.tesco.pma.colleague.profile.service.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesco.pma.colleague.profile.domain.ProfileAttribute;
import com.tesco.pma.colleague.profile.service.rest.model.colleague.ColleagueResponse;
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
public class AggregatedColleagueResponse {

    ColleagueResponse colleague;

    ColleagueResponse lineManager;

    List<ProfileAttribute> profileAttributes;

}
