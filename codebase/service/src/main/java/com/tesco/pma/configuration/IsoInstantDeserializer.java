package com.tesco.pma.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static com.tesco.pma.configuration.DateTimeFormatters.ISO_INSTANT_WITH_THREE_DIGIT_MILLISECOND;

/**
 * Deserializer for {@link Instant} type which uses our own format
 */
public class IsoInstantDeserializer extends InstantDeserializer<Instant> { //NOSONAR

    public IsoInstantDeserializer(InstantDeserializer<Instant> base, DateTimeFormatter formatter) {
        super(base, formatter);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return ISO_INSTANT_WITH_THREE_DIGIT_MILLISECOND.parse(parser.getText(), Instant::from);
    }
}