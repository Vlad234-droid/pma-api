package com.tesco.pma.fs.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.fs.domain.ProcessTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.ProcessTemplateStatus.ACTIVE;
import static com.tesco.pma.fs.domain.ProcessTemplateType.FORM;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/fs/dao/";

    private static final UUID TEMPLATE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID TEMPLATE_UUID_2 = UUID.fromString("3d1ebbee-bafc-467c-acf1-5334db06e723");
    private static final String PATH = "/home/dev";

    @Autowired
    private TemplateDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "process_template_init.xml")
    void findByUuid() {
        final var result = instance.findByUuid(TEMPLATE_UUID_1, true);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(TEMPLATE_UUID_1);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "process_template_init.xml")
    void findAll() {
        final var result = instance.findAll(false);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "process_template_init.xml")
    void getMaxVersion() {
        final var result = instance.getMaxVersion(PATH, "test1.txt");

        assertThat(result).isEqualTo(2);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "cleanup.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "process_template_create_expected.xml")
    void saveSucceeded() {
        final var template = new ProcessTemplate();
        template.setUuid(TEMPLATE_UUID_2);
        template.setPath(PATH);
        template.setVersion(1);
        template.setType(FORM);
        template.setStatus(ACTIVE);
        template.setDescription("other template");
        template.setCreatedBy("test");
        template.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        template.setFileName("test3.txt");
        template.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        template.setFileLength(0);
        template.setFileContent(new byte[] {});

        final int rowsInserted = instance.save(template);

        assertThat(rowsInserted).isOne();
    }
}