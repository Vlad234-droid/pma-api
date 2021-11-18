package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.UploadMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.tesco.pma.fs.dao.FileDAO;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.FileStatus.ACTIVE;
import static com.tesco.pma.fs.domain.FileType.FORM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    void uploadThrowsExceptionWhenDaoReturnsNotOne() throws IOException {
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