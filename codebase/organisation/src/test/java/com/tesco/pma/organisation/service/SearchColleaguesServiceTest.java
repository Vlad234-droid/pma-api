package com.tesco.pma.organisation.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SearchColleaguesServiceTest {

    private final UserService userService = Mockito.mock(UserService.class);
    private final ConfigEntryDAO configEntryDAO = Mockito.mock(ConfigEntryDAO.class);
    private final NamedMessageSourceAccessor messageSourceAccessor = Mockito.mock(NamedMessageSourceAccessor.class);

    private final SearchColleaguesService searchColleaguesService =
            new SearchColleaguesService(userService, configEntryDAO, messageSourceAccessor);

    @Test
    public void getAllSuggestionsTest(){
        List<Colleague> expectedResult = new ArrayList<>();

        Mockito.when(configEntryDAO.findColleagueSuggestionsByFullName(Mockito.anyString(), Mockito.any()))
                .thenReturn(expectedResult);

        var fullNameVal = "FullName";
        var result = searchColleaguesService.getAllSuggestions(fullNameVal);

        assertEquals(expectedResult, result);

        Mockito.verify(configEntryDAO, Mockito.times(1))
                .findColleagueSuggestionsByFullName(Mockito.eq(fullNameVal), Mockito.eq(null));
    }

    @Test
    public void getAllSuggestionsTest_fullname_splitted(){
        List<Colleague> expectedResult = new ArrayList<>();

        Mockito.when(configEntryDAO.findColleagueSuggestions(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(expectedResult);

        var fullNameVal = "Full Name";
        var result = searchColleaguesService.getAllSuggestions(fullNameVal);

        assertEquals(expectedResult, result);

        Mockito.verify(configEntryDAO, Mockito.times(1))
                .findColleagueSuggestions(
                        Mockito.eq("Full"), Mockito.eq(null), Mockito.eq("Name"), Mockito.eq(null));
    }

    @Test
    public void getAllSuggestionsTest_fullname_middlename_splitted(){
        List<Colleague> expectedResult = new ArrayList<>();

        Mockito.when(configEntryDAO.findColleagueSuggestions(
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(expectedResult);

        var fullNameVal = "Full Middle Name";
        var result = searchColleaguesService.getAllSuggestions(fullNameVal);

        assertEquals(expectedResult, result);

        Mockito.verify(configEntryDAO, Mockito.times(1))
                .findColleagueSuggestions(
                        Mockito.eq("Full"), Mockito.eq("Middle"), Mockito.eq("Name"), Mockito.eq(null));
    }

}
