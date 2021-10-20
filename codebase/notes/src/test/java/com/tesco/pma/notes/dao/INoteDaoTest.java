package com.tesco.pma.notes.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.model.NoteStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class INoteDaoTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "db_init_scripts/";
    private static final UUID FOLDER_UUID = UUID.fromString("56141037-6e2d-45f0-b47f-4875e68dd1d7");
    private static final UUID OWNER_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final UUID NOTE_UUID = UUID.fromString("f0977373-5afe-4410-b3a4-ef5b16d7d272");

    @Autowired
    private INoteDao noteDao;

    @Autowired
    private IFolderDao folderDao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml"})
    void create() {

        Note note = new Note();
        note.setId(UUID.randomUUID());
        note.setFolderUuid(FOLDER_UUID);
        note.setOwnerColleagueUuid(OWNER_UUID);
        note.setTitle("Title");
        note.setContent("Content");
        note.setStatus(NoteStatus.CREATED);
        note.setUpdateDate(OffsetDateTime.now());

        var noteCreatedCount = noteDao.create(note);

        assertEquals(1, noteCreatedCount);

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml",
            BASE_PATH_TO_DATA_SET + "notes_entries_init.xml"})
    void deleteFolderWithNoteReferenced() {
        assertEquals(1, noteDao.findByOwnerColleagueUuid(OWNER_UUID).size());

        var folderDeleted = folderDao.delete(FOLDER_UUID);

        assertEquals(1, folderDeleted);
        assertEquals(0, noteDao.findByOwnerColleagueUuid(OWNER_UUID).size());

    }


}
