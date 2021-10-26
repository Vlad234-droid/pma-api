package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonIgnoreProperties({"id", "code"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Role implements DictionaryItem<Integer> {

    private Integer id;

    @JsonProperty("roleName")
    private String name;

    @JsonProperty("roleID")
    private String roleId;

    @JsonProperty("roleDescription")
    private String description;

    @JsonProperty("roleOwner")
    private String owner;

    @JsonProperty("parentRoleId")
    private String parentRoleId;

    @Override
    public String getCode() {
        return String.valueOf(id);
    }

}


