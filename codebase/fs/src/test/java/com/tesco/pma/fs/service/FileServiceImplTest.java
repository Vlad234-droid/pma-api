package com.tesco.pma.fs.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.DictionaryDAO;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.file.api.FileType;
import com.tesco.pma.fs.dao.FileDAO;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.UploadMetadata;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.file.api.FileStatus.ACTIVE;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Sort.SortOrder.DESC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = FileServiceImpl.class)
@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final String FILE_NAME = "test1.txt";
    private static final String FILE_NAME_2 = "test2.txt";
    private static final Integer VERSION_1 = 1;
    private static final Integer VERSION_2 = 2;
    private static final UUID CREATOR_ID = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65767");
    private static final String PATH = "/home/dev";

    @Autowired
    private FileServiceImpl service;

    @MockBean
    private FileDAO fileDao;

    @MockBean
    private DictionaryDAO dictionaryDAO;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Test
    void upload() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var uploadMetadata = new UploadMetadata();
        when(fileDao.create(any())).thenReturn(1);
        when(fileDao.read(any(), eq(false), eq(CREATOR_ID))).thenReturn(fileData);

        var result = service.upload(fileData, uploadMetadata, CREATOR_ID);

        assertNotNull(result);
        assertEquals(FILE_NAME, result.getFileName());
    }

    @Test
    void uploadWithFileNameInMetadata() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var uploadMetadata = new UploadMetadata();
        uploadMetadata.setFileName(FILE_NAME_2);
        when(fileDao.create(any())).thenReturn(1);
        when(fileDao.read(any(), eq(false), eq(CREATOR_ID))).thenReturn(fileData);

        var result = service.upload(fileData, uploadMetadata, CREATOR_ID);

        assertNotNull(result);
        assertEquals(FILE_NAME_2, result.getFileName());
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
        when(fileDao.read(FILE_UUID_1, false, CREATOR_ID)).thenReturn(fileData);

        var result = service.get(FILE_UUID_1, false, CREATOR_ID);

        assertEquals(fileData, result);
    }

    @Test
    void getByUuidThrowsExceptionWhenDaoReturnsNull() {
        when(fileDao.read(FILE_UUID_1, true, CREATOR_ID)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.get(FILE_UUID_1, true, CREATOR_ID));
    }

    @Test
    void getByRequestQuery() {
        var filesData = asList(buildFileData(FILE_NAME, FILE_UUID_1, 1));
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        when(fileDao.findByRequestQuery(requestQuery, includeFileContent, CREATOR_ID, true)).thenReturn(filesData);

        var result = service.get(requestQuery, includeFileContent, CREATOR_ID, true);

        assertEquals(filesData, result);
    }

    @Test
    void getByRequestQueryReturnsNothingWhenDaoFindsNothing() {
        var includeFileContent = true;
        when(fileDao.findByRequestQuery(any(RequestQuery.class), eq(includeFileContent), eq(CREATOR_ID), eq(true))).thenReturn(emptyList());

        var result = service.get(new RequestQuery(), includeFileContent, CREATOR_ID, true);

        assertEquals(emptyList(), result);
    }

    @Test
    void getByFileNameAndPath() {
        var fileData = buildFileData(FILE_NAME, FILE_UUID_1, 1);
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        when(fileDao.findByRequestQuery(eq(requestQuery), eq(includeFileContent), eq(CREATOR_ID), eq(true)))
                .thenReturn(asList(fileData));

        var result = service.get(PATH, FILE_NAME, includeFileContent, CREATOR_ID);

        assertEquals(fileData, result);
    }

    @Test
    void getByFileNameAndPathThrowsExceptionWhenDaoReturnsNull() {
        var includeFileContent = true;
        when(fileDao.findByRequestQuery(any(), eq(includeFileContent), eq(CREATOR_ID), eq(true))).thenReturn(emptyList());

        assertThrows(NotFoundException.class, () -> service.get("/not/existed", "not_existed_file.txt", includeFileContent, CREATOR_ID));
    }

    @Test
    void getAllVersionsByFileNameAndPath() {
        var filesData = asList(buildFileData(FILE_NAME, FILE_UUID_1, 1), buildFileData(FILE_NAME, FILE_UUID_1, 2));
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort("version", DESC)));
        when(fileDao.findByRequestQuery(eq(requestQuery), eq(includeFileContent), eq(CREATOR_ID), eq(false))).thenReturn(filesData);

        var result = service.getAllVersions(PATH, FILE_NAME, includeFileContent, CREATOR_ID);

        assertEquals(filesData, result);
    }

    @Test
    void getAllVersionsReturnsNothingWhenDaoFindsNothing() {
        var includeFileContent = false;
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(new Condition("path", EQUALS, PATH), new Condition("file-name", EQUALS, FILE_NAME)));
        requestQuery.setLimit(null);
        requestQuery.setSort(Arrays.asList(new Sort("version", DESC)));
        when(fileDao.findByRequestQuery(eq(requestQuery), eq(includeFileContent), eq(CREATOR_ID), eq(false))).thenReturn(emptyList());

        var result = service.getAllVersions(PATH, FILE_NAME, includeFileContent, CREATOR_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteFileByUuids() {
        when(fileDao.deleteByUuidAndColleague(FILE_UUID_1, CREATOR_ID)).thenReturn(1);

        service.delete(FILE_UUID_1, CREATOR_ID);

        verify(fileDao, times(1)).deleteByUuidAndColleague(FILE_UUID_1, CREATOR_ID);
    }

    @Test
    void deleteFileByUuidsThrowsNotFoundExceptionWhenDaoReturnsNotOne() {
        when(fileDao.deleteByUuidAndColleague(CREATOR_ID, FILE_UUID_1)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> service.delete(FILE_UUID_1, CREATOR_ID));
    }

    @Test
    void deleteFileByVersions() {
        when(fileDao.deleteVersions(PATH, FILE_NAME, VERSION_1, CREATOR_ID)).thenReturn(1);
        when(fileDao.deleteVersions(PATH, FILE_NAME, VERSION_2, CREATOR_ID)).thenReturn(1);

        service.deleteVersions(PATH, FILE_NAME, List.of(VERSION_1, VERSION_2), CREATOR_ID);

        verify(fileDao, times(1)).deleteVersions(PATH, FILE_NAME, VERSION_1, CREATOR_ID);
        verify(fileDao, times(1)).deleteVersions(PATH, FILE_NAME, VERSION_2, CREATOR_ID);
    }

    @Test
    void deleteFileByVersionsThrowsNotFoundExceptionWhenDaoReturnsNotOne() {
        when(fileDao.deleteVersions(PATH, FILE_NAME, VERSION_1, CREATOR_ID)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> service.deleteVersions(PATH, FILE_NAME, List.of(VERSION_1, VERSION_2), CREATOR_ID));
    }

    private File buildFileData(String fileName, UUID uuid, Integer version) {
        final var fileData = new File();
        fileData.setUuid(uuid);
        fileData.setPath(PATH);
        fileData.setVersion(version);
        FileType type = new FileType();
        type.setId(2);
        type.setCode("FORM");
        type.setDescription("GUI Form file");
        fileData.setType(type);
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