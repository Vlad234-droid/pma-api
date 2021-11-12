package com.tesco.pma.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class,
        properties = {"tesco.application.rest-template.security.enabled=false"})
class DateTimeTypesSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("provideArgsForSerializer")
    void whenSerializeDateTimeTypesThenCorrectOutput(Object inputDateTime,
                                                            String expected) throws JsonProcessingException {
        // Act
        String result = objectMapper.writeValueAsString(inputDateTime);

        // Assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideArgsForSerializer() { // NOPMD
        return Stream.of(
                // Instant type with custom IsoInstantSerializer
                Arguments.of(
                        Instant.parse("2021-04-28T23:59:59Z"),
                        "\"2021-04-28T23:59:59.000Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T23:59:59.3Z"),
                        "\"2021-04-28T23:59:59.300Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T23:59:59.342679Z"),
                        "\"2021-04-28T23:59:59.342Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T23:59:59.345Z"),
                        "\"2021-04-28T23:59:59.345Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T23:59:59.000Z"),
                        "\"2021-04-28T23:59:59.000Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T00:00:00.000Z"),
                        "\"2021-04-28T00:00:00.000Z\""),
                Arguments.of(
                        Instant.parse("2021-04-28T00:00:00Z"),
                        "\"2021-04-28T00:00:00.000Z\""),

                // LocalDateTime type
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T23:59:59"),
                        "\"2021-04-28T23:59:59.000\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T23:59:59.3"),
                        "\"2021-04-28T23:59:59.300\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-29T16:35:03.736068800"),
                        "\"2021-04-29T16:35:03.736\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T23:59:59.345"),
                        "\"2021-04-28T23:59:59.345\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T23:59:59.000"),
                        "\"2021-04-28T23:59:59.000\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T00:00:00.000"),
                        "\"2021-04-28T00:00:00.000\""),
                Arguments.of(
                        LocalDateTime.parse("2021-04-28T00:00:00"),
                        "\"2021-04-28T00:00:00.000\""),

                // LocalTime type
                Arguments.of(
                        LocalTime.parse("18:04:49.4140638"),
                        "\"18:04:49.414\""),
                Arguments.of(
                        LocalTime.parse("18:04:49.41"),
                        "\"18:04:49.410\""),
                Arguments.of(
                        LocalTime.parse("18:04:49.414"),
                        "\"18:04:49.414\""),
                Arguments.of(
                        LocalTime.parse("18:04:49.00"),
                        "\"18:04:49.000\""),
                Arguments.of(
                        LocalTime.parse("00:00:00"),
                        "\"00:00:00.000\""),

                // ZonedDateTime type
                Arguments.of(
                        ZonedDateTime.parse("2021-04-29T20:46:43.1697712Z[Etc/UTC]"),
                        "\"2021-04-29T20:46:43.169Z[Etc/UTC]\""),
                Arguments.of(
                        ZonedDateTime.parse("2021-04-29T20:46:43.16Z[Etc/UTC]"),
                        "\"2021-04-29T20:46:43.160Z[Etc/UTC]\""),
                Arguments.of(
                        ZonedDateTime.parse("2021-04-29T20:46:43.169Z[Etc/UTC]"),
                        "\"2021-04-29T20:46:43.169Z[Etc/UTC]\""),
                Arguments.of(
                        ZonedDateTime.parse("2021-04-29T00:00:00Z[Etc/UTC]"),
                        "\"2021-04-29T00:00:00.000Z[Etc/UTC]\""),

                // OffsetDateTime type
                Arguments.of(
                        OffsetDateTime.parse("2021-04-29T21:10:42.941907Z"),
                        "\"2021-04-29T21:10:42.941Z\""),
                Arguments.of(
                        OffsetDateTime.parse("2021-04-29T21:10:42.94Z"),
                        "\"2021-04-29T21:10:42.940Z\""),
                Arguments.of(
                        OffsetDateTime.parse("2021-04-29T21:10:42.941Z"),
                        "\"2021-04-29T21:10:42.941Z\""),
                Arguments.of(
                        OffsetDateTime.parse("2021-04-29T00:00:00.00Z"),
                        "\"2021-04-29T00:00:00.000Z\""),
                Arguments.of(
                        OffsetDateTime.parse("2021-04-29T00:00:00Z"),
                        "\"2021-04-29T00:00:00.000Z\""),

                // OffsetTime type
                Arguments.of(
                        OffsetTime.parse("08:27:23.2695579Z"),
                        "\"08:27:23.269Z\""),
                Arguments.of(
                        OffsetTime.parse("08:27:23.26Z"),
                        "\"08:27:23.260Z\""),
                Arguments.of(
                        OffsetTime.parse("08:27:23.269Z"),
                        "\"08:27:23.269Z\""),
                Arguments.of(
                        OffsetTime.parse("00:00:00.00Z"),
                        "\"00:00:00.000Z\""),
                Arguments.of(
                        OffsetTime.parse("00:00:00Z"),
                        "\"00:00:00.000Z\"")
        );
    }
}