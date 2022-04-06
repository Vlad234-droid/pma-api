package com.tesco.pma.notes.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.notes.model.Folder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoldersDAOTest extends AbstractDAOTest {

    protected static final String BASE_PATH_TO_DATA_SET = "db_init_scripts/";
    protected static final UUID FOLDER_UUID = UUID.fromString("56141037-6e2d-45f0-b47f-4875e68dd1d7");
    protected static final UUID OWNER_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    protected static final UUID OWNER_UUID_2 = UUID.fromString("3d14c7dd-867c-4996-9f16-378435298e58");
    protected static final UUID NOTE_UUID = UUID.fromString("f0977373-5afe-4410-b3a4-ef5b16d7d272");

    @Autowired
    private NotesDAO notesDao;

    @Autowired
    private FoldersDAO foldersDao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml"})
    void create() {

        var parent = createFolder(UUID.randomUUID(), OWNER_UUID, null);
        foldersDao.create(parent);

        var folder = createFolder(UUID.randomUUID(), OWNER_UUID, parent.getId());
        var folderCreatedCount = foldersDao.create(folder);

        assertEquals(1, folderCreatedCount);

        assertEquals(parent.getId(),
                foldersDao.findByOwner(OWNER_UUID).stream()
                        .filter(f -> folder.getId().equals(f.getId()))
                        .findAny().get().getParentFolderUuid());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml",
            BASE_PATH_TO_DATA_SET + "notes_entries_init.xml"})
    void createDuplicate() {
        var folder = createFolder(FOLDER_UUID, OWNER_UUID, null);

        Assertions.assertThrows(DuplicateKeyException.class, () -> foldersDao.create(folder));

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml"})
    void update() {

        var folder = createFolder(FOLDER_UUID, OWNER_UUID, null);
        folder.setTitle("New Title");
        var folderCreatedCount = foldersDao.update(folder);

        assertEquals(1, folderCreatedCount);

        assertEquals("New Title", foldersDao.findByOwner(OWNER_UUID).stream()
                .filter(f -> folder.getId().equals(FOLDER_UUID))
                .findAny().get().getTitle());

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml",
            BASE_PATH_TO_DATA_SET + "notes_entries_init.xml"})
    void deleteFolderWithNoteReferenced() {
        assertEquals(1, notesDao.findByOwner(OWNER_UUID).size());

        var folderDeleted = foldersDao.delete(FOLDER_UUID);

        assertEquals(1, folderDeleted);
        assertEquals(0, notesDao.findByOwner(OWNER_UUID).size());

    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "folder_entries_init.xml"})
    void findByOwnerColleagueUuid(){

        var folders = foldersDao.findByOwner(OWNER_UUID_2);

        assertEquals(1, folders.size());

    }

    private Folder createFolder(UUID id, UUID ownerId, UUID parentId){
        var folder = new Folder();
        folder.setId(id);
        folder.setOwnerColleagueUuid(ownerId);
        folder.setTitle("Title");
        folder.setParentFolderUuid(parentId);
        return folder;
    }


}
