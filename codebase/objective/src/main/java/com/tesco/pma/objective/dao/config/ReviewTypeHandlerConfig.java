package com.tesco.pma.objective.dao.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.objective.dao.serializer.ReviewPropertiesJsonbCombinedSerializer;
import com.tesco.pma.objective.dao.utils.ReviewPropertiesJsonbTypeHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewTypeHandlerConfig {

    @Bean
    public ReviewPropertiesJsonbTypeHandler reviewPropertiesJsonbTypeHandler(@Qualifier("jsonbObjectMapper") ObjectMapper objectMapper) {

        return new ReviewPropertiesJsonbTypeHandler(configureSerializer(objectMapper, new ReviewPropertiesJsonbCombinedSerializer()
                .getSimpleModule()));
    }

    private ObjectMapper configureSerializer(ObjectMapper objectMapper, Module module) {
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
