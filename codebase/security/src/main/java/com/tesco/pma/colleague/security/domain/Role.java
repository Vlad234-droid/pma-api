package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tesco.pma.api.DictionaryItem;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Used to give the user access to a particular resource under a particular role.
 *
 * <p>For more information:
 *  @see <a href="https://github.dev.global.tesco.org/97-TeamTools/Colleague-Authentication-and-Access/wiki/Data-Collections">here</a>
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role implements DictionaryItem<Integer> {

    @JsonProperty("roleId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer id;

    @JsonProperty("roleName")
    private String code;

    @JsonProperty("roleDesc")
    private String description;

}


