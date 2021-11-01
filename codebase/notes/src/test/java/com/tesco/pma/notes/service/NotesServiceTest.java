package com.tesco.pma.notes.service;

import com.tesco.pma.api.User;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.dao.FolderDao;
import com.tesco.pma.notes.dao.NoteDao;
import com.tesco.pma.notes.exception.NoteIntegrityException;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.UUID;

public class NotesServiceTest {

    private final FolderDao folderDao = Mockito.mock(FolderDao.class);
    private final NoteDao noteDao = Mockito.mock(NoteDao.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final NamedMessageSourceAccessor messageSourceAccessor = Mockito.mock(NamedMessageSourceAccessor.class);
    private final ConfigEntryDAO configEntryDAO = Mockito.mock(ConfigEntryDAO.class);

    private final NotesService notesService = new NotesService(folderDao, noteDao, userService, messageSourceAccessor, configEntryDAO);

    private final UUID currentUserUuid = UUID.randomUUID();
    private User currentUser;


    @BeforeEach
    public void init(){
        currentUser = new User();
        currentUser.setColleagueUuid(currentUserUuid);
    }

    @Test
    public void createNoteForNotCurrentUserTest(){
        Mockito.when(userService.currentUser(Mockito.any()))
                .thenReturn(Optional.of(currentUser));

        var note = new Note();
        note.setOwnerColleagueUuid(UUID.randomUUID());

        Assertions.assertThrows(BadCredentialsException.class, () -> {
            notesService.createNote(note);
        });

    }

    @Test
    public void createNoteUnauthenticatedTest(){
        Mockito.when(userService.currentUser(Mockito.any()))
                .thenReturn(Optional.ofNullable(null));

        var note = new Note();
        note.setOwnerColleagueUuid(UUID.randomUUID());

        Assertions.assertThrows(NotFoundException.class, () -> {
            notesService.createNote(note);
        });

    }

    @Test
    public void createNoteWithWrongReferenceTest(){
        Mockito.when(userService.currentUser(Mockito.any()))
                .thenReturn(Optional.of(currentUser));

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
        Mockito.when(userService.currentUser(Mockito.any()))
                .thenReturn(Optional.of(currentUser));

        var note = new Note();
        note.setOwnerColleagueUuid(currentUser.getColleagueUuid());
        note.setReferenceColleagueUuid(currentUser.getColleagueUuid());

        Assertions.assertThrows(NoteIntegrityException.class, () -> {
            notesService.createNote(note);
        });

    }
}
