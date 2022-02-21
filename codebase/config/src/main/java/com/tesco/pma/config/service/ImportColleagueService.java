package com.tesco.pma.config.service;

import com.tesco.pma.config.domain.ImportError;
import com.tesco.pma.config.domain.ImportReport;
import com.tesco.pma.config.domain.ImportRequest;
import com.tesco.pma.config.domain.ImportRequestStatus;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ImportColleagueService {

    /**
     * Start import colleague process
     *
     * @param inputStream - input stream of excel file
     * @param fileName    - file name
     * @return import error
     */
    ImportReport importColleagues(InputStream inputStream, String fileName);

    /**
     * Get request by uuid
     *
     * @param requestUuid - request identifier
     * @return import request object
     */
    ImportRequest getRequest(UUID requestUuid);

    /**
     * Get import errors for request
     *
     * @param requestUuid - request identifier
     * @return list of errors
     */
    List<ImportError> getRequestErrors(UUID requestUuid);

    /**
     * Creates import request
     *
     * @param fileName - fileName
     * @return import request object
     */
    ImportRequest createImportRequest(String fileName);

    /**
     * Stored list of errors into db
     *
     * @param errors - errors
     */
    void saveErrors(Collection<ImportError> errors);

    /**
     * Updates request status
     *
     * @param request - request object
     * @param status  - new status
     */
    void updateRequestStatus(ImportRequest request, ImportRequestStatus status);
}
