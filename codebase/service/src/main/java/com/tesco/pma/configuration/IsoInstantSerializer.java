package com.tesco.pma.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

import static com.tesco.pma.configuration.DateTimeFormatters.ISO_INSTANT_WITH_THREE_DIGIT_MILLISECOND;

/**
 * Serializer for {@link Instant} type which uses with three-digit millisecond
 */
public class IsoInstantSerializer extends JsonSerializer<Instant> {

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String formattedInstant = ISO_INSTANT_WITH_THREE_DIGIT_MILLISECOND.format(instant);
        jsonGenerator.writeString(formattedInstant);
    }

    @Override
    public Class<Instant> handledType() {
        return Instant.class;
    }
}