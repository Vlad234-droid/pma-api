package com.tesco.pma.bpm.camunda.util;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-10 21:50
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
class ExtensionsDelegateVariableMappingTest extends AbstractCamundaSpringBootTest {

    private static final String FLOW_ID = "delegate_variable_mapping";

    private static final String CALL_EYR = "call_review_init_eyr";
    private static final String MYR = "myr";
    private static final String CALL_MYR = "call_review_init_myr";

    private static final String EYR_EXP_FILE = "eyr-expected.properties";
    private static final String MYR_EXP_FILE = "myr-expected.properties";

    private static final String EYR_FILE = "eyr.properties";
    private static final String MYR_FILE = "myr.properties";


    enum FlowParams {
        TARGET_PATH,
        TARGET_FILE_NAME
    }

    @TempDir
    static Path tempDir;

    @Test
    void mapInputVariables() {
        var tempPath = tempDir.toFile().getAbsolutePath();
        assertThatForProcess(runProcess(FLOW_ID, Map.of(FlowParams.TARGET_PATH.name(), tempPath)))
                .activity(CALL_EYR).executedOnce()
                .activity(MYR).executedOnce()
                .activity(CALL_MYR).executedOnce();

        Map.of(EYR_EXP_FILE, EYR_FILE, MYR_EXP_FILE, MYR_FILE).forEach((expected, actual) -> {
            checkProperties(loadProperties(expected), loadProperties(Paths.get(tempDir.toString(), actual)));
        });
    }

    private Properties loadProperties(String resource) {
        var props = new Properties();
        try (var fi = getClass().getResourceAsStream(resource)) {
            props.load(fi);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        return props;
    }

    private Properties loadProperties(Path path) {
        var props = new Properties();
        try (var fi = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            props.load(fi);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        return props;
    }

    private void checkProperties(Properties expected, Properties actual) {
        expected.forEach((key, value) -> {
            Assertions.assertTrue(actual.containsKey(key), (String) key);
            Assertions.assertEquals(value, actual.get(key), (String) key);
        });
    }
}