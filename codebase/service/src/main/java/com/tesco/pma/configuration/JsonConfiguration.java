package com.tesco.pma.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static com.tesco.pma.configuration.DateTimeFormatters.ISO_LOCAL_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND;
import static com.tesco.pma.configuration.DateTimeFormatters.ISO_LOCAL_TIME_WITH_THREE_DIGIT_MILLISECOND;
import static com.tesco.pma.configuration.DateTimeFormatters.ISO_OFFSET_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND;
import static com.tesco.pma.configuration.DateTimeFormatters.ISO_OFFSET_TIME_WITH_THREE_DIGIT_MILLISECOND;
import static com.tesco.pma.configuration.DateTimeFormatters.ISO_ZONED_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

/**
 * Jackson Object Mapper config.
 */
@Configuration
public class JsonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .serializers(
                        new LocalDateSerializer(ISO_LOCAL_DATE),
                        new LocalTimeSerializer(ISO_LOCAL_TIME_WITH_THREE_DIGIT_MILLISECOND),
                        new LocalDateTimeSerializer(ISO_LOCAL_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND),
                        new ZonedDateTimeSerializer(ISO_ZONED_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND),
                        new IsoOffsetDateTimeSerializer(false, true,
                                ISO_OFFSET_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND),
                        new IsoOffsetTimeSerializer(false, true,
                                ISO_OFFSET_TIME_WITH_THREE_DIGIT_MILLISECOND),
                        new IsoInstantSerializer()
                )
                .failOnUnknownProperties(false);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
    static class JacksonObjectMapperConfiguration {

        /**
         * Creates primary object mapper bean
         *
         * @param builder - object mapper builder
         * @return object mapper bean
         */
        @Bean
        @Primary
        ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
            return builder.createXmlMapper(false).build();
        }

    }
}
