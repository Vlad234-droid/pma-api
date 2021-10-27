package com.tesco.pma.feedback.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.api.FeedbackTargetType;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeedbackDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/feedback/dao/";
    private static final UUID FEEDBACK_UUID_UNREAD = UUID.fromString("b5eedbaa-d313-449e-880e-640e6446304a");
    private static final UUID FEEDBACK_UUID_LAST = UUID.fromString("7ed4dd94-143d-4e66-93b9-1f9cd0f3a1fd");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("be245be1-1f43-4d5f-85dc-db6e2cce0c2a");
    private static final String TARGET_UUID_INSERT = "cb1b76c1-31f6-45c3-a783-034ae7aed871";
    private static final UUID FEEDBACK_ITEM_UUID = UUID.fromString("693b16a7-e151-486a-b4cd-3b377dddac95");

    @Autowired
    private FeedbackDAO underTest;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldFindAllFeedbacksWithItems() {
        //given
        RequestQuery requestQuery = new RequestQuery();

        //when
        List<Feedback> result = underTest.findAll(requestQuery);

        //then
        assertThat(result)
                .hasSize(2)
                .element(0)
                .returns(FEEDBACK_UUID_LAST, Feedback::getUuid);
        assertEquals(3, result.get(0).getFeedbackItems().size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldGetOneFeedbackByUuidWithItems() {
        //given

        //when
        Feedback result = underTest.getByUuid(FEEDBACK_UUID_LAST);

        //then
        assertThat(result)
                .returns(FEEDBACK_UUID_LAST, Feedback::getUuid)
                .returns(COLLEAGUE_UUID, Feedback::getColleagueUuid)
                .returns(FeedbackTargetType.OBJECTIVE, Feedback::getTargetType);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldMarkAsRead() {
        //given

        //when
        int result = underTest.markAsRead(FEEDBACK_UUID_UNREAD);
        Feedback readFeedback = underTest.getByUuid(FEEDBACK_UUID_UNREAD);

        //then
        assertEquals(1, result);
        assertTrue(readFeedback.getRead());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldInsertFeedback() {
        //given
        Feedback feedback = new Feedback();
        feedback.setUuid(UUID.randomUUID());
        feedback.setColleagueUuid(COLLEAGUE_UUID);
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setTargetType(FeedbackTargetType.OTHER);
        feedback.setTargetId(TARGET_UUID_INSERT);
        feedback.setCreatedTime(Instant.now());

        //when
        int result = underTest.insert(feedback);

        assertEquals(1, result);
    }

}
