package com.tesco.pma.feedback.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackItemDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/feedback/dao/";

    @Autowired
    private FeedbackItemDAO underTest;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void insert() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(UUID.randomUUID());

        //when
        int result = underTest.insert(feedbackItem);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void update() {
        //given
        FeedbackItem feedbackItem = TestDataUtil.buildFeedbackItem();
        feedbackItem.setUuid(TestDataUtil.FEEDBACK_ITEM_UUID);

        //when
        int result = underTest.update(feedbackItem);

        //then
        assertEquals(1, result);
    }
}