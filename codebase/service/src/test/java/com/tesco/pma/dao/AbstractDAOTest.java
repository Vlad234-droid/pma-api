package com.tesco.pma.dao;

import com.github.database.rider.junit5.api.DBRider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Testcontainers
@DBRider
public abstract class AbstractDAOTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDAOTest.class);

    static Properties properties;
    static {
        properties = new Properties();
        var url = AbstractDAOTest.class.getClassLoader().getResource("config/application-test.yml");
        try (var inputStream = new FileInputStream(url.getPath())) { // NOPMD
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Failed to load properties");
        }
    }

    // TODO: datasource can be different rather than Postgresql only, so, container should be configured accordantly.
    @Container
    static final
    PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag(getPostgresVersion()));

    // TODO: version must be corresponded to the datasource.
    static String getPostgresVersion() {
        return properties.getProperty("postgres-version");
    }
}
