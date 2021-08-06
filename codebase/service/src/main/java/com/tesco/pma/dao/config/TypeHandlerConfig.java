package com.tesco.pma.dao.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tesco.pma.dao.utils.UuidTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TypeHandlerConfig {

    @Bean
    public UuidTypeHandler uuidTypeHandler() {
        return new UuidTypeHandler();
    }

    /**
     * Creates json object mapper bean for JSONB db format
     *
     * @return object mapper bean
     */
    @Bean
    public ObjectMapper jsonbObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return objectMapper;
    }

    private ObjectMapper configureSerializer(ObjectMapper objectMapper, Module module) {
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
