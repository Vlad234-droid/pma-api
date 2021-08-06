package com.tesco.pma.configuration;

import com.fasterxml.jackson.datatype.jsr310.ser.OffsetTimeSerializer;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializer for {@link OffsetTime} type with needed DateTimeFormatter
 */
public class IsoOffsetTimeSerializer extends OffsetTimeSerializer { //NOSONAR used third-party inheritance tree

    public IsoOffsetTimeSerializer(Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter) {
        super(INSTANCE, useTimestamp, useNanoseconds, formatter);
    }

    @Override
    public Class<OffsetTime> handledType() {
        return OffsetTime.class;
    }
}