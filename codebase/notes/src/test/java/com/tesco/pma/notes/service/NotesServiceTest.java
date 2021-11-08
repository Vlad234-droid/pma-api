package com.tesco.pma.notes.service;

import com.tesco.pma.api.User;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.notes.dao.FoldersDao;
import com.tesco.pma.notes.dao.NotesDao;
import com.tesco.pma.notes.exception.NoteIntegrityException;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.UUID;

public class NotesServiceTest {

    private final FoldersDao foldersDao = Mockito.mock(FoldersDao.class);
    private final NotesDao notesDao = Mockito.mock(NotesDao.class);
    private final NamedMessageSourceAccessor messageSourceAccessor = Mockito.mock(NamedMessageSourceAccessor.class);
    private final ConfigEntryDAO configEntryDAO = Mockito.mock(ConfigEntryDAO.class);

    private final NotesService notesService = new NotesService(foldersDao, notesDao, messageSourceAccessor, configEntryDAO);

    private final UUID currentUserUuid = UUID.randomUUID();
    private User currentUser;


    @BeforeEach
    public void init(){
        currentUser = new User();
        currentUser.setColleagueUuid(currentUserUuid);
    }

    @Test
    public void createNoteWithWrongReferenceTest(){

        UUID referenceUuid = UUID.randomUUID();

        var colleague = new Colleague();
        colleague.setUuid(referenceUuid);
        colleague.setManagerUuid(UUID.randomUUID());

        Mockito.when(configEntryDAO.getColleague(referenceUuid)).thenReturn(colleague);

        var note = new Note();
        note.setOwnerColleagueUuid(currentUser.getColleagueUuid());
        note.setReferenceColleagueUuid(referenceUuid);

        Assertions.assertThrows(NoteIntegrityException.class, () -> {
            notesService.createNote(note);
        });

    }

    @Test
    public void createNoteWithOwnerEqualsReferenceTest(){

        var note = new Note();
        note.setOwnerColleagueUuid(currentUser.getColleagueUuid());
        note.setReferenceColleagueUuid(currentUser.getColleagueUuid());

        Assertions.assertThrows(NoteIntegrityException.class, () -> {
            notesService.createNote(note);
        });

    }
}
