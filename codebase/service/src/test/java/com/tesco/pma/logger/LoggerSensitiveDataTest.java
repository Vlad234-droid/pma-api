package com.tesco.pma.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.tesco.pma.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class LoggerSensitiveDataTest {

    public static final String DEFAULT_INFO_LOG = "This is default INFO log";
    public static final String EMAIL_HIDING_LOG = "This is email log with email: test_email@test.com";
    public static final String EMAIL_HIDING_LOG_EXPECTED = "This is email log with email: *******************";
    public static final String PASSWORD_HIDING_LOG = "This is log with password test or password:test_password";
    public static final String PASSWORD_HIDING_LOG_EXPECTED = "This is log with password **** or password:*************";

    private Logger logger;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = lc.getLogger("ROOT");
    }

    @Test
    void testDefaultMessage() {
        testLog(DEFAULT_INFO_LOG, () -> logger.info(DEFAULT_INFO_LOG));
    }

    @Test
    void testSensitiveEmail() {
        testLog(EMAIL_HIDING_LOG_EXPECTED, () -> logger.info(EMAIL_HIDING_LOG));
    }

    @Test
    void testSensitivePassword() {
        testLog(PASSWORD_HIDING_LOG_EXPECTED, () -> logger.info(PASSWORD_HIDING_LOG));
    }

    private void testLog(String expectedMessage, Runnable logRunner) {

        logRunner.run();
        String actual = outputStreamCaptor.toString().trim();
        assertTrue(actual.endsWith(expectedMessage));
    }
}
