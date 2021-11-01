package com.tesco.pma.notes.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.dao.FolderDao;
import com.tesco.pma.notes.dao.NoteDao;
import com.tesco.pma.notes.exception.NotesErrorCodes;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class NotesService {

    private final FolderDao folderDao;
    private final NoteDao noteDao;
    private final UserService userService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Transactional
    public Note createNote(Note note){
        log.debug("Creating a Note {} for {}", note.getTitle(), note.getOwnerColleagueUuid().toString());
        //TODO check if the current user is a line manager of the reference colleague (if present)

        checkCurrentUser(note.getOwnerColleagueUuid());
        note.setId(UUID.randomUUID());

        if (0 == noteDao.create(note)) {
            throw new RuntimeException("Note " + note.getId() + " hasn't been created");
        }

        return note;
    }

    public List<Note> findNoteByOwner(UUID uuid){
       return noteDao.findByOwner(uuid);
    }

    @Transactional
    public Note updateNote(Note note){
        checkCurrentUser(note.getOwnerColleagueUuid());

        if (1 == noteDao.update(note)) {
            return note;
        }

        throw new NotFoundException(NotesErrorCodes.NOTE_NOT_FOUND.getCode(),
                messageSourceAccessor.getMessage(NotesErrorCodes.NOTE_NOT_FOUND, Map.of("id", note.getId())));
    }

    @Transactional
    public void deleteNote(UUID uuid){
        noteDao.delete(uuid);
    }

    @Transactional
    public Folder createFolder(Folder folder){
        log.debug("Creating folder {} for {}", folder.getTitle(), folder.getOwnerColleagueUuid().toString());
        checkCurrentUser(folder.getOwnerColleagueUuid());
        folder.setId(UUID.randomUUID());

        if (0 == folderDao.create(folder)) {
            throw new RuntimeException("Folder " + folder.getId() + " hasn't been created");
        }

        return folder;
    }

    public List<Folder> findFolderByOwner(UUID uuid){
        return folderDao.findByOwner(uuid);
    }

    @Transactional
    public Folder updateFolder(Folder folder){
        checkCurrentUser(folder.getOwnerColleagueUuid());

        if (1 == folderDao.update(folder)) {
            return folder;
        }

        throw new NotFoundException(NotesErrorCodes.FOLDER_NOT_FOUND.getCode(),
                messageSourceAccessor.getMessage(NotesErrorCodes.FOLDER_NOT_FOUND, Map.of("id", folder.getId())));
    }

    @Transactional
    public void deleteFolder(UUID uuid){
        folderDao.delete(uuid);
    }

    private void checkCurrentUser(UUID colleagueUuid){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        var user = userService.findUserByAuthentication(authentication, EnumSet.of(UserIncludes.SUBSIDIARY_PERMISSIONS)) //TODO includes?
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_FOUND,
                                Map.of("param_name", "colleague ID", "param_value", colleagueUuid))));

        if(!user.getColleagueUuid().equals(colleagueUuid)){
            throw new BadCredentialsException(messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_AUTHORIZED));
        }
    }

}
