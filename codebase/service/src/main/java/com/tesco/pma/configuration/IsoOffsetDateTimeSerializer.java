package com.tesco.pma.configuration;

import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializer for {@link OffsetDateTime} type with needed DateTimeFormatter
 */
public class IsoOffsetDateTimeSerializer extends OffsetDateTimeSerializer { //NOSONAR used third-party inheritance tree

    public IsoOffsetDateTimeSerializer(Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter) {
        super(INSTANCE, useTimestamp,  useNanoseconds,  formatter);
    }

    @Override
    public Class<OffsetDateTime> handledType() {
        return OffsetDateTime.class;
    }
}