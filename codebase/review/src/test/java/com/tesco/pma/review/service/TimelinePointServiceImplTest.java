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

import java.util.List;

import static com.tesco.pma.review.util.TestDataUtils.TL_POINT_UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void get() {
        //given
        var timelinePoint = TestDataUtils.buildTimelinePoint();
        when(dao.getByParams(any(), any(), any())).thenReturn(List.of(timelinePoint));

        //when
        var timelinePoints = underTest.get(null, null, null);

        //then
        verify(dao, times(1)).getByParams(null, null, null);
        assertEquals(TL_POINT_UUID, timelinePoints.get(0).getUuid());
    }

    @Test
    void updateStatus() {
        //given
        when(dao.updateStatus(any(), any(), any())).thenReturn(1);

        //when
        var status = underTest.updateStatus(null, null, null);

        //then
        verify(dao, times(1)).updateStatus(null, null, null);
        assertEquals(1, status);
    }

}
