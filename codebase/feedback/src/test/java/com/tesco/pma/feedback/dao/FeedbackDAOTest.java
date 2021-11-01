package com.tesco.pma.feedback.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackStatus;
import com.tesco.pma.feedback.api.FeedbackTargetType;
import com.tesco.pma.feedback.util.TestDataUtil;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeedbackDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/feedback/dao/";

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
                .returns(TestDataUtil.FEEDBACK_UUID_LAST, Feedback::getUuid);
        assertEquals(3, result.get(0).getFeedbackItems().size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldGetOneFeedbackByUuidWithItems() {
        //given

        //when
        Feedback result = underTest.getByUuid(TestDataUtil.FEEDBACK_UUID_LAST);

        //then
        assertThat(result)
                .returns(TestDataUtil.FEEDBACK_UUID_LAST, Feedback::getUuid)
                .returns(TestDataUtil.COLLEAGUE_UUID, Feedback::getColleagueUuid)
                .returns(FeedbackTargetType.OBJECTIVE, Feedback::getTargetType);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldMarkAsRead() {
        //given

        //when
        int result = underTest.markAsRead(TestDataUtil.FEEDBACK_UUID_UNREAD);
        Feedback readFeedback = underTest.getByUuid(TestDataUtil.FEEDBACK_UUID_UNREAD);

        //then
        assertEquals(1, result);
        assertTrue(readFeedback.getRead());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldInsertFeedback() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(UUID.randomUUID());

        //when
        int result = underTest.insert(feedback);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldUpdateFeedback() {
        //given
        DictionaryFilter<FeedbackStatus> statusFilter = DictionaryFilter.includeFilter();
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);

        //when
        int result = underTest.update(feedback, statusFilter);

        //then
        assertEquals(1, result);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "feedback_init.xml"})
    void shouldThrowDuplicateKeyExceptionWhenInsertFeedbackHasAlreadyExistsUuid() {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);

        //when and then
        assertThatCode(() -> underTest.insert(feedback))
                .isExactlyInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining(TestDataUtil.FEEDBACK_UUID_LAST.toString());
    }

}
