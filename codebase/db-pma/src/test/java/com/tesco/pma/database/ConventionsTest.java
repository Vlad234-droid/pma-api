package com.tesco.pma.database;

import io.restassured.path.xml.XmlPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConventionsTest {

    private static final String CHANGELOG_MASTER_FILE = System.getProperty("CHANGELOG_MASTER_FILE");

    private static final Pattern CHANGELOG_FILE_NAME_PATTERN = Pattern.compile(System.getProperty("CHANGELOG_FILENAME_PATTERN"));

    @ParameterizedTest(name = "{displayName} [{index}], file {0}")
    @MethodSource("provideChangeLogFiles")
    void changeLogFileNamesMustFollowPattern(Path changeLogPath) {
        Assertions.assertThat(changeLogPath.getFileName().toString())
                .matches(CHANGELOG_FILE_NAME_PATTERN);
    }

    @ParameterizedTest(name = "{displayName} [{index}] for file {0}")
    @MethodSource("provideChangeLogFiles")
    void changeSetIdsMustBeIntegerAndOrdered(Path changeLogPath) {
        final var changeSetIds = XmlPath.from(changeLogPath.toFile()).getList("databaseChangeLog.changeSet.@id", String.class);
        Assertions.assertThat(changeSetIds).isNotEmpty()
                .allMatch(id -> {
                    try {
                        Integer.parseInt(id);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }, "must be Integer")
                .map(Integer::parseInt)
                .doesNotHaveDuplicates()
                .isSorted();
    }

    @ParameterizedTest(name = "{displayName} [{index}], file {0}")
    @MethodSource("provideIntegrationDataChangeLogFiles")
    void integrationChangeSetsMustHaveLabel(Path integrationDataChangeLogPath) {
        final var changeSets = XmlPath.from(integrationDataChangeLogPath.toFile()).getList("databaseChangeLog.changeSet");
        final var changeSetsWithLabelsAttribute = XmlPath.from(integrationDataChangeLogPath.toFile())
                .getList("databaseChangeLog.changeSet.@labels", String.class);
        Assertions.assertThat(changeSets.size()).isEqualTo(changeSetsWithLabelsAttribute.size());
        Assertions.assertThat(changeSetsWithLabelsAttribute).allMatch("integration"::equals,
                "labels attribute must be equal to integration");
    }

    @ParameterizedTest(name = "{displayName} [{index}], file {0}")
    @MethodSource("providePerformanceTestingDataChangeLogFiles")
    void performanceTestingChangeSetsMustHaveLabel(Path performanceTestingDataChangeLogPath) {
        final var changeSets = XmlPath.from(performanceTestingDataChangeLogPath.toFile()).getList("databaseChangeLog.changeSet");
        final var changeSetsWithLabelsAttribute = XmlPath.from(performanceTestingDataChangeLogPath.toFile())
                .getList("databaseChangeLog.changeSet.@labels", String.class);
        Assertions.assertThat(changeSets.size()).isEqualTo(changeSetsWithLabelsAttribute.size());
        Assertions.assertThat(changeSetsWithLabelsAttribute).allMatch("performance"::equals,
                "labels attribute must be equal to integration");
    }

    static Stream<Path> provideChangeLogFiles() throws Exception {
        final var walkPath = Path.of(ConventionsTest.class.getResource(CHANGELOG_MASTER_FILE).toURI()).getParent();
        return Files.walk(walkPath)
                .filter(not(Files::isDirectory))
                .filter(path -> path.getFileName().toString().endsWith(".xml"))
                .filter(not(path -> CHANGELOG_MASTER_FILE.endsWith(path.getFileName().toString())));
    }

    static Stream<Path> provideIntegrationDataChangeLogFiles() throws Exception {

        final var databasePath = Path.of(ConventionsTest.class.getResource(CHANGELOG_MASTER_FILE).toURI()).getParent();
        final var walkPath = Paths.get(databasePath.toString(), "integration-data");
        return Files.walk(walkPath)
                .filter(not(Files::isDirectory))
                .filter(path -> path.getFileName().toString().endsWith(".xml"));
    }

    static Stream<Path> providePerformanceTestingDataChangeLogFiles() throws Exception {

        final var databasePath = Path.of(ConventionsTest.class.getResource(CHANGELOG_MASTER_FILE).toURI()).getParent();
        final var walkPath = Paths.get(databasePath.toString(), "performance-testing-data");
        return Files.walk(walkPath)
                .filter(not(Files::isDirectory))
                .filter(path -> path.getFileName().toString().endsWith(".xml"));
    }

}
