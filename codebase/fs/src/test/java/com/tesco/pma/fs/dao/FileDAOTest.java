package com.tesco.pma.fs.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.fs.domain.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.FileStatus.ACTIVE;
import static com.tesco.pma.fs.domain.FileType.FORM;
import static org.assertj.core.api.Assertions.assertThat;

public class FileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/fs/dao/";

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID FILE_UUID_2 = UUID.fromString("3d1ebbee-bafc-467c-acf1-5334db06e723");
    private static final String PATH = "/home/dev";

    @Autowired
    private FileDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void readByUuid() {
        final var result = instance.read(FILE_UUID_1, true);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(FILE_UUID_1);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "cleanup.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "file_create_expected.xml")
    void createSucceeded() {
        final File file = buildFileData();

        final int rowsInserted = instance.create(file);

        assertThat(rowsInserted).isOne();
    }

    private File buildFileData() {
        final var fileData = new File();
        fileData.setUuid(FILE_UUID_2);
        fileData.setPath(PATH);
        fileData.setVersion(1);
        fileData.setType(FORM);
        fileData.setStatus(ACTIVE);
        fileData.setDescription("other file");
        fileData.setCreatedBy("test");
        fileData.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        fileData.setFileName("test3.txt");
        fileData.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        fileData.setFileLength(0);
        fileData.setFileContent(new byte[] {});
        return fileData;
    }
}