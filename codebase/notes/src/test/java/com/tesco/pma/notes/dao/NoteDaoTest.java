package com.tesco.pma.notes.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.model.NoteStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NoteDaoTest extends AbstractDAOTest {

    protected static final String BASE_PATH_TO_DATA_SET = "db_init_scripts/";
    protected static final UUID FOLDER_UUID = UUID.fromString("56141037-6e2d-45f0-b47f-4875e68dd1d7");
    protected static final UUID OWNER_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    protected static final UUID NOTE_UUID = UUID.fromString("f0977373-5afe-4410-b3a4-ef5b16d7d272");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Autowired
    private NoteDao noteDao;

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml"})
    void create() {

        var note = createNote(UUID.randomUUID(), FOLDER_UUID, OWNER_UUID);
        var noteCreatedCount = noteDao.create(note);

        assertEquals(1, noteCreatedCount);

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml",
            BASE_PATH_TO_DATA_SET + "notes_entries_init.xml"})
    void createDuplicate() {

        Assertions.assertThrows(DuplicateKeyException.class, () -> {
            var note = createNote(NOTE_UUID, FOLDER_UUID, OWNER_UUID);
            noteDao.create(note);
        });

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml",
            BASE_PATH_TO_DATA_SET + "notes_entries_init.xml"})
    void update() {

        var note = createNote(NOTE_UUID, FOLDER_UUID, OWNER_UUID);
        note.setTitle("New Title");
        var noteCreatedCount = noteDao.update(note);

        assertEquals(1, noteCreatedCount);

        assertEquals("New Title", noteDao.findByOwner(OWNER_UUID).stream()
                .filter(f -> note.getId().equals(NOTE_UUID))
                .findAny().get().getTitle());

    }

    private Note createNote(UUID id, UUID folderId, UUID ownerId){
        var note = new Note();
        note.setId(id);
        note.setFolderUuid(folderId);
        note.setOwnerColleagueUuid(ownerId);
        note.setTitle("Title");
        note.setContent("Content");
        note.setStatus(NoteStatus.CREATED);
        note.setUpdateDate(OffsetDateTime.now());
        return note;
    }


}
