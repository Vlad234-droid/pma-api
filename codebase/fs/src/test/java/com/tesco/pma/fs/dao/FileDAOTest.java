package com.tesco.pma.fs.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.FileType;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.pagination.Sort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.file.api.FileStatus.ACTIVE;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.GREATER_THAN;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class FileDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/fs/dao/";

    private static final UUID FILE_UUID = UUID.fromString("3d1ebbee-bafc-467c-acf1-5334db06e723");
    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID FILE_UUID_2 = UUID.fromString("ede76e9d-deb9-47b9-83b3-cf2a57b8baeb");
    private static final UUID FILE_UUID_3 = UUID.fromString("e0f57a75-0bae-405e-9952-76c9c60aa6a3");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("3d1ebbee-bafc-467c-acf1-5334db06e723");
    private static final UUID NOT_OWNER_COLLEAGUE_UUID = UUID.fromString("5d1ebbee-bafc-467c-acf1-5334db06e723");
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
        final var result = instance.read(FILE_UUID_1, true, COLLEAGUE_UUID);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(FILE_UUID_1);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void readByUuidFoundNothingIfColleagueIsNotOwner() {
        final var result = instance.read(FILE_UUID_1, true, NOT_OWNER_COLLEAGUE_UUID);

        assertThat(result).isNull();
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithSearch() {
        final var requestQuery = new RequestQuery();
        requestQuery.setSearch("test2");

        final var result = instance.findByRequestQuery(requestQuery, true, COLLEAGUE_UUID, true);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(FILE_UUID_3);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithFilters() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(Condition.build("type", 2), Condition.build("status", 2)));

        final var result = instance.findByRequestQuery(requestQuery, true, COLLEAGUE_UUID, true);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(FILE_UUID_3);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithFilter() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("description", IN, asList("another files")),
                                       new Condition("status", IN, asList("2"))));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, true);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(FILE_UUID_3);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithFilterByUuid() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("uuid", EQUALS, FILE_UUID_2)));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, true);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUuid()).isEqualTo(FILE_UUID_2);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithFilterFoundNothingIfNoMatchedData() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("file-length", GREATER_THAN, 15)));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, true);

        assertThat(result).isEmpty();
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithFilterFoundNothingIfColleagueIsNotOwner() {
        final var result = instance.findByRequestQuery(new RequestQuery(),
                emptyList(), true, NOT_OWNER_COLLEAGUE_UUID, true);

        assertThat(result).isEmpty();
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryWithSorting() {
        final var requestQuery = new RequestQuery();
        requestQuery.setSort(asList(Sort.build("file-length:ASC")));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, true);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUuid()).isEqualTo(FILE_UUID_3);
        assertThat(result.get(1).getUuid()).isEqualTo(FILE_UUID_2);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryGetAllVersions() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH),
                                       new Condition("file-name", EQUALS, "test1.txt")));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, false);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "file_init.xml")
    void findByRequestQueryGetAllVersionsReturnsNothing() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH),
                                       new Condition("file-name", EQUALS, "not_existed_file.txt")));

        final var result = instance.findByRequestQuery(requestQuery,
                 true, COLLEAGUE_UUID, false);

        assertThat(result).isEmpty();
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "cleanup.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "file_create_expected.xml")
    void createSucceeded() {
        final var file = buildFileData();

        final var rowsInserted = instance.create(file);

        assertThat(rowsInserted).isOne();
    }

    private File buildFileData() {
        final var fileData = new File();
        fileData.setUuid(FILE_UUID);
        fileData.setPath(PATH);
        fileData.setVersion(1);
        FileType type = new FileType();
        type.setId(2);
        type.setCode("FORM");
        type.setDescription("GUI Form file");
        fileData.setType(type);
        fileData.setStatus(ACTIVE);
        fileData.setDescription("other file");
        fileData.setCreatedBy(UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765"));
        fileData.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        fileData.setFileName("test3.txt");
        fileData.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        fileData.setFileLength(0);
        fileData.setFileContent(new byte[]{});
        return fileData;
    }
}