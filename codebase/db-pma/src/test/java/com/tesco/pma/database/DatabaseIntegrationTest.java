package com.tesco.pma.database;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

@Testcontainers
class DatabaseIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag("11.11"));

    private static final String CHANGELOG_MASTER_FILE = System.getProperty("CHANGELOG_MASTER_FILE");

    private static Liquibase liquibase;

    @BeforeAll
    static void beforeAll() throws Exception {
        try (var resourceAccessor = new ClassLoaderResourceAccessor()) {
            final var database = DatabaseFactory.getInstance().openDatabase(
                    CONTAINER.getJdbcUrl(),
                    CONTAINER.getUsername(),
                    CONTAINER.getPassword(),
                    Properties.class.getName(),
                    resourceAccessor
            );

            liquibase = new Liquibase(CHANGELOG_MASTER_FILE, resourceAccessor, database);
        }
    }

    @Test
    void testUpdateRollbackChanges() throws Exception { //NOSONAR used for tesing liquibase scripts on test container
        String testContext = "test";
        liquibase.tag("initial-tag");
        liquibase.update("before-changes", testContext);
        if (liquibase.tagExists("before-changes")) {
            liquibase.updateTestingRollback(testContext);
        } else {
            liquibase.rollback("initial-tag", testContext);
            liquibase.update(testContext);
        }
    }

}
