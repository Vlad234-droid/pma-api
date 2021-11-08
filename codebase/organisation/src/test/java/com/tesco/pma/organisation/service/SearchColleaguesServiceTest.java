package com.tesco.pma.organisation.service;

import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SearchColleaguesServiceTest {

    private final ConfigEntryDAO configEntryDAO = Mockito.mock(ConfigEntryDAO.class);

    private final SearchColleaguesService searchColleaguesService = new SearchColleaguesService(configEntryDAO);

    @Test
    public void getAllSuggestionsTest(){
        List<Colleague> expectedResult = new ArrayList<>();

        Mockito.when(configEntryDAO.findColleagueSuggestionsByFullName(Mockito.anyString(), Mockito.any()))
                .thenReturn(expectedResult);

        var fullNameVal = "FullName";
        var result = searchColleaguesService.getSuggestions(fullNameVal, null);

        assertEquals(expectedResult, result);

        Mockito.verify(configEntryDAO, Mockito.times(1))
                .findColleagueSuggestionsByFullName(Mockito.eq(fullNameVal), Mockito.eq(null));
    }

}
