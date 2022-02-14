package com.tesco.pma.cms.service;

import com.tesco.pma.cms.dao.ContentDAO;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceTest {

    @Mock
    private ContentDAO contentDAO;

    @InjectMocks
    private ContentServiceImpl contentService;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void findByRequestQueryTest(){

        var rq = new RequestQuery();
        rq.addFilters("type_eq", "knowledge-library");
        rq.addFilters("countryCode_eq", "GB");
        rq.addFilters("iam_eq", "iam1");
        rq.addFilters("manager_eq", "manager");

        contentService.findByRequestQuery(rq);

        Mockito.verify(contentDAO).findByKey(Mockito.argThat(key -> {
            assertEquals("knowledge-library/GB/iam1/manager", key);
            return true;
        }));
    }

}
