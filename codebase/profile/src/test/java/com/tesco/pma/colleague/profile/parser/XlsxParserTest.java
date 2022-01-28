package com.tesco.pma.colleague.profile.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-28 11:56
 */
class XlsxParserTest {

    private static final int LINE_COUNT = 7;

    @Test
    void skipEmptyLines() throws Exception {
        try (var fis = getClass().getResourceAsStream("test-parser.xlsx")) {
            var parser = new XlsxParser();
            assertNotNull(fis);
            var result = parser.parse(fis);
            assertTrue(result.isSuccess());
            assertEquals(LINE_COUNT, result.getData().size());
        }
    }
}