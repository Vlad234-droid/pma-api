package com.tesco.pma.fs.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.api.RequestQueryToDictionaryFilterConverter;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.api.FileStatus;
import com.tesco.pma.fs.api.FileType;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.pagination.Sort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.tesco.pma.fs.dao.FileDAO;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static com.tesco.pma.fs.api.FileStatus.ACTIVE;
import static com.tesco.pma.fs.api.FileStatus.INACTIVE;
import static com.tesco.pma.fs.api.FileType.BPMN;
import static com.tesco.pma.fs.api.FileType.FORM;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Sort.SortOrder.DESC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(classes = FileServiceImpl.class)
@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final String FILE_NAME = "test1.txt";
    private static final UUID CREATOR_ID = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65767");
    private static final String PATH = "/home/dev";

    @Autowired
    private FileServiceImpl service;

    @MockBean
    private FileDAO fileDao;

    @MockBean
    private RequestQueryToDictionaryFilterConverter toDictionaryFilterConverter;

    @Test
    void upload() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var uploadMetadata = new UploadMetadata();
        when(fileDao.create(any())).thenReturn(1);
        when(fileDao.read(any(), eq(false))).thenReturn(fileData);

        var result = service.upload(fileData, uploadMetadata, CREATOR_ID);

        assertNotNull(result);
        assertEquals(FILE_NAME, result.getFileName());
    }

    @Test
    void uploadThrowsExceptionWhenDaoReturnsNotOne() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var uploadMetadata = new UploadMetadata();
        when(fileDao.create(fileData)).thenReturn(-1);

        assertThrows(RegistrationException.class, () -> service.upload(fileData, uploadMetadata, CREATOR_ID));
    }

    @Test
    void getByUuid() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        when(fileDao.read(FILE_UUID_1, false)).thenReturn(fileData);

        var result = service.get(FILE_UUID_1, false);

        assertEquals(fileData, result);
    }

    @Test
    void getByUuidThrowsExceptionWhenDaoReturnsNull() {
        when(fileDao.read(FILE_UUID_1, true)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.get(FILE_UUID_1, true));
    }

    @Test
    void getByRequestQuery() {
        var filesData = asList(buildFileData(FILE_NAME, FILE_UUID_1, 1));
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        var includeStatusFilter = DictionaryFilter.includeFilter(ACTIVE);
        var excludeStatusFilter = DictionaryFilter.excludeFilter(INACTIVE);
        var includeTypeFilter = DictionaryFilter.includeFilter(FORM);
        var excludeTypeFilter = DictionaryFilter.excludeFilter(BPMN);
        when(toDictionaryFilterConverter.convert(requestQuery, true, "status", FileStatus.class))
                .thenReturn(includeStatusFilter);
        when(toDictionaryFilterConverter.convert(requestQuery, false, "status", FileStatus.class))
                .thenReturn(excludeStatusFilter);
        when(toDictionaryFilterConverter.convert(requestQuery, true, "type", FileType.class))
                .thenReturn(includeTypeFilter);
        when(toDictionaryFilterConverter.convert(requestQuery, false, "type", FileType.class))
                .thenReturn(excludeTypeFilter);
        when(fileDao.findByRequestQuery(requestQuery, asList(includeStatusFilter, excludeStatusFilter),
                asList(includeTypeFilter, excludeTypeFilter), includeFileContent, true)).thenReturn(filesData);

        var result = service.get(requestQuery, includeFileContent, true);

        assertEquals(filesData, result);
    }

    @Test
    void getByRequestQueryReturnsNothingWhenDaoFindsNothing() {
        var includeFileContent = true;
        var requestQuery = new RequestQuery();
        when(fileDao.findByRequestQuery(any(RequestQuery.class), anyList(), anyList(), eq(includeFileContent), eq(true)))
                .thenReturn(emptyList());

        var result = service.get(requestQuery, includeFileContent, true);

        assertEquals(emptyList(), result);
    }

    @Test
    void getByFileNameAndPath() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        when(fileDao.findByRequestQuery(eq(requestQuery), any(), any(), eq(includeFileContent), eq(true))).thenReturn(asList(fileData));

        var result = service.get(PATH, FILE_NAME, includeFileContent);

        assertEquals(fileData, result);
    }

    @Test
    void getByFileNameAndPathThrowsExceptionWhenDaoReturnsNull() {
        var includeFileContent = true;
        when(fileDao.findByRequestQuery(any(), any(), any(), eq(includeFileContent), eq(true))).thenReturn(emptyList());

        assertThrows(NotFoundException.class, () -> service.get("/not/existed", "not_existed_file.txt", includeFileContent));
    }

    @Test
    void getAllVersionsByFileNameAndPath() {
        var filesData = asList(buildFileData(FILE_NAME, FILE_UUID_1, 1), buildFileData(FILE_NAME, FILE_UUID_1, 2));
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort("version", DESC)));
        when(fileDao.findByRequestQuery(eq(requestQuery), any(), any(), eq(includeFileContent), eq(false))).thenReturn(filesData);

        var result = service.getAllVersions(PATH, FILE_NAME, includeFileContent);

        assertEquals(filesData, result);
    }

    @Test
    void getAllVersionsReturnsNothingWhenDaoFindsNothing() {
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort("version", DESC)));
        when(fileDao.findByRequestQuery(eq(requestQuery), any(), any(), eq(includeFileContent), eq(false))).thenReturn(emptyList());

        var result = service.getAllVersions(PATH, FILE_NAME, includeFileContent);

        assertThat(result).isEmpty();
    }

    private File buildFileData(String fileName, UUID uuid, Integer version) {
        final var fileData = new File();
        fileData.setUuid(uuid);
        fileData.setPath(PATH);
        fileData.setVersion(version);
        fileData.setType(FORM);
        fileData.setStatus(ACTIVE);
        fileData.setDescription("other file");
        fileData.setCreatedBy(CREATOR_ID);
        fileData.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        fileData.setFileName(fileName);
        fileData.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        fileData.setFileLength(3);
        fileData.setFileContent(new byte[] {72, 101, 108});

        return fileData;
    }
}