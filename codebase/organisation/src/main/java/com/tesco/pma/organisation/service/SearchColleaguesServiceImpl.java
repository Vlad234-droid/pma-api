package com.tesco.pma.organisation.service;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchColleaguesServiceImpl implements SearchColleaguesService {

    private final ConfigEntryDAO configEntryDAO;

    @Override
    public List<Colleague> getSuggestions(String fullName, UUID managerId){
        var names = Arrays.stream(fullName.split(StringUtils.SPACE))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return configEntryDAO.findColleagueSuggestionsByFullName(names, managerId);
    }

}
