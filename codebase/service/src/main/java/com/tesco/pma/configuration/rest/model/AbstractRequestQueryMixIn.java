package com.tesco.pma.configuration.rest.model;

import com.tesco.pma.pagination.RequestQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RequestQuery", example = "{\n"
        + "    \"_sort\": \"field1:DESC,field2:ASC\",\n"
        + "    \"field1\": \"A\",\n"
        + "    \"field2_ne\": \"5\",\n"
        + "    \"field3_contains\": \"A\",\n"
        + "    \"field4_ncontains\": \"B\",\n"
        + "    \"field5_in\": [\"A\",\"B\"],\n"
        + "    \"field6_in[0]\": \"A\",\n"
        + "    \"field6_in[1]\": \"B\",\n"
        + "    \"field6_in[2]\": \"C\",\n"
        + "    \"field7_nin\": [\"C\",\"D\"],\n"
        + "    \"field8_lt\": \"5\",\n"
        + "    \"field9_lte\": \"5\",\n"
        + "    \"field10_gt\": \"5\",\n"
        + "    \"field11_gte\": \"5\",\n"
        + "    \"field12_null\": \"true\",\n"
        + "    \"_groups\": [{ \n"
        + "             \"_type\": \"OR\",\n"
        + "             \"field2_eq\": \"B\",\n"
        + "             \"field3_eq\": \"C\"}],\n"
        + "    \"_start\": \"1\",\n"
        + "    \"_limit\": \"7\",\n"
        + "    \"_search\": \"A\"\n"
        + "  }")
public abstract class AbstractRequestQueryMixIn extends RequestQuery {

}