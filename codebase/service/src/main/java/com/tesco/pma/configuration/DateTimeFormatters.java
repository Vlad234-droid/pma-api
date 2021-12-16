package com.tesco.pma.configuration;

import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * The ISO-8601 compatible DateTime Formatters with three-digit milliseconds used for DateTime types, Time types and Instant
 */
@UtilityClass
public class DateTimeFormatters { //NOPMD

    private static final int DIGITS_MILLISECOND_COUNT = 3;
    private static final String ISO_INSTANT_PATTERN_WITH_OPTIONAL_LAST_PART = "yyyy-MM-dd'T'HH:mm:ss[.SSS]['Z']";

    /**
     * The ISO-8601 compatible formatter for LocalTime that formats or parses a LocalTime.
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_LOCAL_TIME}
     * but with 3-digit millisecond
     * Applied format is "HH:mm:ss.SSS"
     */
    static final DateTimeFormatter ISO_LOCAL_TIME_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendFraction(NANO_OF_SECOND, DIGITS_MILLISECOND_COUNT, DIGITS_MILLISECOND_COUNT, true)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(null);

    /**
     * The ISO-8601 compatible formatter for LocalDateTime that formats or parses a LocalDateTime.
     * LocalDateTime is a date-time which has no time-zone.
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME}
     * but with 3-digit millisecond and without optional parts in {@link java.time.format.DateTimeFormatter.ISO_LOCAL_TIME} part of it.
     * Applied format is "yyyy-MM-dd'T'HH:mm:ss.SSS"
     */
    static final DateTimeFormatter ISO_LOCAL_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(ISO_LOCAL_TIME_WITH_THREE_DIGIT_MILLISECOND)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    /**
     * The ISO-8601 compatible formatter for Instant that formats or parses an instant in UTC.
     * Applied format is "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_INSTANT} but with 3-digit millisecond
     */
    static final DateTimeFormatter ISO_INSTANT_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(ISO_INSTANT_PATTERN_WITH_OPTIONAL_LAST_PART).toFormatter()
            .withResolverStyle(ResolverStyle.SMART)
            .withZone(ZoneOffset.UTC)
            .withChronology(null);

    /**
     * The ISO-8601 compatible formatter for ZonedDateTime that formats or parses a OffsetDateTime.
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME}
     * but with 3-digit millisecond
     * Ex. of applying this format in UTC timezone            : 2021-04-29T21:04:41.373Z
     * Ex. of applying this format in Europe/Helsinki timezone: 2021-04-30T00:06:13.883+03:00
     */
    static final DateTimeFormatter ISO_OFFSET_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND)
            .parseLenient()
            .appendOffsetId()
            .parseStrict()
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);


    /**
     * The ISO-8601 compatible formatter for ZonedDateTime that formats or parses a ZonedDateTime.
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME}
     * but with 3-digit millisecond
     * Ex. of applying this format in UTC timezone            : 2021-04-29T20:46:43.169Z[Etc/UTC]
     * Ex. of applying this format in Europe/Helsinki timezone: 2021-04-29T23:44:48.898+03:00[Europe/Helsinki]
     */
    static final DateTimeFormatter ISO_ZONED_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .append(ISO_OFFSET_DATE_TIME_WITH_THREE_DIGIT_MILLISECOND)
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId()
            .appendLiteral(']')
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    /**
     * The ISO-8601 compatible formatter for OffsetTime that formats or parses a OffsetTime.
     * This formatter is similar to {@link java.time.format.DateTimeFormatter.ISO_OFFSET_TIME}
     * but with 3-digit millisecond and without optional parts in {@link java.time.format.DateTimeFormatter.ISO_LOCAL_TIME} part of it.
     * Ex. of applying this format in UTC timezone            : 08:27:23.269Z
     * Ex. of applying this format in Europe/Helsinki timezone: 11:26:27.887+03:00
     */
    static final DateTimeFormatter ISO_OFFSET_TIME_WITH_THREE_DIGIT_MILLISECOND = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_TIME_WITH_THREE_DIGIT_MILLISECOND)
            .appendOffsetId()
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(null);
}