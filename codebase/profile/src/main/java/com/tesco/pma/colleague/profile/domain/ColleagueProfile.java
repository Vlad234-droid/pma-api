package com.tesco.pma.colleague.profile.domain;

import com.tesco.pma.colleague.api.Colleague;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Profile model.
 */
@Data
@NoArgsConstructor
public class ColleagueProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Colleague colleague;
    private List<TypedAttribute> profileAttributes;
}
