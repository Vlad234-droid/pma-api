package com.tesco.pma.organisation.service;

import com.tesco.pma.colleague.api.Colleague;

import java.util.List;
import java.util.UUID;

public interface SearchColleaguesService {

    List<Colleague> getSuggestions(String fullName, UUID managerId);

}
