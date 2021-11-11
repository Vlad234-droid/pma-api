package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.domain.ImportRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ImportColleagueService {

    ImportReport importColleagues(InputStream inputStream, String fileName);

    ImportRequest getRequest(UUID requestUuid);

    List<ImportError> getRequestErrors(UUID requestUuid);
}
