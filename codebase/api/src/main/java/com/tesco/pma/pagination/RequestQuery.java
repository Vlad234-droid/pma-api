package com.tesco.pma.pagination;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class is used to describe next JSON object:
 * {
 *   "_sort": "created_at:desc",
 *   "_publicationState": "preview",
 *   "published_at_null": "false",
 *   "_start": "5",
 *   "_limit": "20",
 *   "_search": "String",
 * }
 * _where section wil be ignored, since we don't need to use OR operand
 */
@Data
@JsonIgnoreProperties("_where")
public class RequestQuery {

    @JsonProperty("_start")
    private Integer offset;

    @JsonProperty("_limit")
    private Integer limit;

    private List<Sort> sort;

    private List<Condition> filters;

    @JsonProperty("_search")
    private String search;

    public RequestQuery() {
        this.sort = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    @JsonProperty("_sort")
    public void setSortString(String sort) {
        if (StringUtils.isNotEmpty(sort)) {
            this.sort = Arrays.stream(sort.split(","))
                    .map(Sort::build)
                    .collect(Collectors.toList());
        }
    }

    @JsonAnySetter
    public void addFilters(String name, Object value) {
        this.filters.add(Condition.build(name, value));
    }

    @JsonIgnore
    public Set<String> getFiltersProperties() {
        if (this.filters == null) {
            return new HashSet<>();
        }

        return filters.stream().map(Condition::getProperty).collect(Collectors.toSet());
    }

    @JsonIgnore
    public static RequestQuery create(Map<String, Object> filters) {
        var rq = new RequestQuery();

        if(filters == null){
            return rq;
        }

        filters.forEach(rq::addFilters);
        return rq;
    }

    @JsonIgnore
    public static RequestQuery create(String name, Object value) {
        return create(Map.of(name, value));
    }
}