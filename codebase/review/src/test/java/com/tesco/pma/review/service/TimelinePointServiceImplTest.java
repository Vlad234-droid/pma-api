package com.tesco.pma.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.util.TestDataUtils;
import com.tesco.pma.service.BatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = TimelinePointServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class TimelinePointServiceImplTest {

    @Autowired
    private TimelinePointService underTest;

    @MockBean
    private TimelinePointDAO dao;

    @MockBean
    private BatchService batchService;

    @MockBean
    private NamedMessageSourceAccessor messages;

    @Test
    void create() {
        //given
        var timelinePoint = TestDataUtils.buildTimelinePoint();
        when(dao.create(timelinePoint)).thenReturn(1);

        //when
        underTest.create(timelinePoint);

        //then
        verify(dao, times(1)).create(timelinePoint);
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenCreateWithDuplicateKey() {
        //given
        var timelinePoint = TestDataUtils.buildTimelinePoint();
        when(dao.create(timelinePoint)).thenThrow(DuplicateKeyException.class);

        //then
        assertThrows(AlreadyExistsException.class, () -> underTest.create(timelinePoint));
    }

}
