package com.tesco.pma.organisation.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SearchColleaguesService {

    private final ConfigEntryDAO configEntryDAO;

    public List<Colleague> getSuggestions(String fullName, UUID managerId){
        fullName = fullName.trim();
        return configEntryDAO.findColleagueSuggestionsByFullName(fullName, managerId);
    }

}
