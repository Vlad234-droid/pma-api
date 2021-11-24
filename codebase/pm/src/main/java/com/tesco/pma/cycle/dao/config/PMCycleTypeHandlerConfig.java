package com.tesco.pma.cycle.dao.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.cycle.dao.utils.PMCycleJsonbCombinedSerializer;
import com.tesco.pma.cycle.dao.utils.PMCycleJsonbTypeHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PMCycleTypeHandlerConfig {

    @Bean
    public PMCycleJsonbTypeHandler pmCycleJsonbTypeHandler(@Qualifier("jsonbObjectMapper") ObjectMapper objectMapper) {

        return new PMCycleJsonbTypeHandler(configureSerializer(objectMapper, new PMCycleJsonbCombinedSerializer()
                .getSimpleModule()));
    }

    private ObjectMapper configureSerializer(ObjectMapper objectMapper, Module module) {
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
