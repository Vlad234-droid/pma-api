package com.tesco.pma.tip.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.dao.TipDAO;
import com.tesco.pma.tip.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TipServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class TipServiceTest {

    @Autowired
    private TipService underTest;

    @MockBean
    private TipDAO dao;

    @MockBean
    private NamedMessageSourceAccessor messageSource;

    @Test
    void create() {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY);

        //when
        Tip result = underTest.create(tip);

        //then
        assertNotNull(result.getKey());
    }

    @Test
    void shouldThrowDatabaseConstraintViolationExceptionWhenCreateWithDuplicateKey() {
        //given
        Tip tip = TestDataUtil.buildTip();
        when(dao.create(tip)).thenThrow(DuplicateKeyException.class);

        //then
        assertThrows(DatabaseConstraintViolationException.class, () -> underTest.create(tip));
    }

    @Test
    void findAll() {
        //given
        RequestQuery requestQuery = new RequestQuery();
        Tip tip1 = TestDataUtil.buildTip();
        tip1.setKey(TestDataUtil.TIP_KEY);
        Tip tip2 = TestDataUtil.buildTip();
        tip2.setKey(TestDataUtil.TIP_KEY_UNPUBLISHED);
        List<Tip> tips = List.of(tip1, tip2);
        when(dao.findAll(requestQuery)).thenReturn(tips);

        //when
        List<Tip> result = underTest.findAll(requestQuery);

        //then
        assertThat(result)
                .hasSize(2)
                .element(0)
                .isSameAs(tip1);
    }

    @Test
    void findOne() {
        //given
        Tip tip = TestDataUtil.buildTip();
        when(dao.read(TestDataUtil.TIP_KEY)).thenReturn(tip);

        //when
        Tip result = underTest.findOne(TestDataUtil.TIP_KEY);

        //then
        verify(dao, times(1)).read(TestDataUtil.TIP_KEY);
        assertEquals(TestDataUtil.TARGET_ORGANISATION_UUID, result.getTargetOrganisation().getUuid());
    }

    @Test
    void update() {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY);
        tip.setTitle(TestDataUtil.TIP_TITLE);
        when(dao.read(TestDataUtil.TIP_KEY)).thenReturn(tip);
        when(dao.create(tip)).thenReturn(1);

        //when
        Tip result = underTest.update(tip);

        //then
        assertEquals(TestDataUtil.TIP_TITLE, result.getTitle());
    }

    @Test
    void delete() {
        //given
        when(dao.delete(TestDataUtil.TIP_KEY)).thenReturn(1);

        //when
        underTest.delete(TestDataUtil.TIP_KEY);

        //then
        verify(dao, times(1)).delete(TestDataUtil.TIP_KEY);
    }

    @Test
    void notFoundException() {
        //given
        when(dao.delete(TestDataUtil.TIP_KEY)).thenReturn(0);

        //then
        assertThrows(NotFoundException.class, () -> underTest.delete(TestDataUtil.TIP_KEY));
    }

    @Test
    void publish() {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY_UNPUBLISHED);
        when(dao.read(TestDataUtil.TIP_KEY_UNPUBLISHED)).thenReturn(tip);

        //when
        underTest.publish(TestDataUtil.TIP_KEY_UNPUBLISHED);

        //then
        verify(dao, times(1)).read(TestDataUtil.TIP_KEY_UNPUBLISHED);
        verify(dao, times(1)).create(tip);
    }
}