package com.tesco.pma.pagination;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConditionGroup {

    @JsonProperty("_type")
    private GroupType type = GroupType.AND;
    private List<Condition> filters = new ArrayList<>();

    @Getter
    public enum GroupType {
        OR, AND
    }

    @JsonAnySetter
    public void addFilters(String name, Object value) {
        this.filters.add(Condition.build(name, value));
    }

}
