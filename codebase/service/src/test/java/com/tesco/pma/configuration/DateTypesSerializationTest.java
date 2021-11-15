package com.tesco.pma.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.TestConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
class DateTypesSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("provideArgsForSerializer")
    void whenSerializeDateTypesThenCorrectOutput(Object inputDateTime,
                                                        String expected) throws JsonProcessingException {
        // Act
        String result = objectMapper.writeValueAsString(inputDateTime);

        // Assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideArgsForSerializer() {
        return Stream.of(
                // LocalDate type
                Arguments.of(
                        LocalDate.parse("2021-04-28"),
                        "\"2021-04-28\""),
                Arguments.of(
                        LocalDate.parse("2021-01-01"),
                        "\"2021-01-01\""),
                Arguments.of(
                        LocalDate.parse("21-04-28", DateTimeFormatter.ofPattern("yy-MM-dd")),
                        "\"2021-04-28\""),
                Arguments.of(
                        LocalDate.parse("2021/04/28", DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                        "\"2021-04-28\"")
        );
    }
}
