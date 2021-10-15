package com.tesco.pma.objective.dao.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.dao.utils.jsonb.JsonbTypeHandler;
import com.tesco.pma.objective.domain.ReviewProperties;

public class ReviewPropertiesJsonbTypeHandler extends JsonbTypeHandler<ReviewProperties> {
    public ReviewPropertiesJsonbTypeHandler(ObjectMapper objectMapper) {
        super(ReviewProperties.class, objectMapper);
    }
}
