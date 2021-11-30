package com.tesco.pma.tip.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TipDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/feedback/dao/";

    @Autowired
    private TipDAO underTest;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "tip_init.xml"})
    void create() {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setPkUuid(UUID.randomUUID());
        tip.setUuid(UUID.randomUUID());
        tip.setTitle("new title");
        tip.setCreatedTime(Instant.now());

        //when
        int result = underTest.create(tip);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "tip_init.xml"})
    void shouldThrowDuplicateKeyExceptionWhenInsertExistingUniqueKey() {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setPkUuid(UUID.randomUUID());
        tip.setUuid(UUID.randomUUID());
        tip.setCreatedTime(Instant.now());

        //when and then
        assertThatCode(() -> underTest.create(tip))
                .isExactlyInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining(TestDataUtil.TIP_TITLE);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "tip_init.xml"})
    void findByRequestQuery() {
        //given
        RequestQuery requestQuery = new RequestQuery();

        //when
        List<Tip> result = underTest.findByRequestQuery(requestQuery);

        //then
        assertThat(result)
                .hasSize(2)
                .element(1)
                .returns(TestDataUtil.TIP_UUID, Tip::getUuid);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "tip_init.xml"})
    void findByUuid() {
        //when
        Tip result = underTest.findByUuid(TestDataUtil.TIP_UUID);

        //then
        assertThat(result)
                .returns(TestDataUtil.TIP_UUID, Tip::getUuid)
                .returns(TestDataUtil.TIP_TITLE, Tip::getTitle)
                .returns(TestDataUtil.TIP_DESCRIPTION, Tip::getDescription)
                .returns(TestDataUtil.TIP_IMAGE_LINK, Tip::getImageLink);
        assertNotNull(result.getTargetOrganisation());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "tip_init.xml"})
    void delete() {
        //when
        int result = underTest.delete(TestDataUtil.TIP_UUID);

        //then
        assertEquals(1, result);
    }
}