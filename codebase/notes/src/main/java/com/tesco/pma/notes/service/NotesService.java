package com.tesco.pma.notes.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.dao.FolderDao;
import com.tesco.pma.notes.dao.NoteDao;
import com.tesco.pma.notes.exception.NoteIntegrityException;
import com.tesco.pma.notes.exception.NotesErrorCodes;
import com.tesco.pma.notes.exception.UnknownDataManipulationException;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.organisation.dao.ConfigEntryDAO;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final ConfigEntryDAO configEntryDAO;

    @Transactional
    public Note createNote(Note note){
        log.debug("Creating a Note {} for {}", note.getTitle(), note.getOwnerColleagueUuid().toString());
        checkCurrentUser(note.getOwnerColleagueUuid());
        checkReferenceColleague(note.getOwnerColleagueUuid(), note.getReferenceColleagueUuid());
        note.setId(UUID.randomUUID());

        try {
            if (0 == noteDao.create(note)) {
                throw new UnknownDataManipulationException(messageSourceAccessor.getMessage(NotesErrorCodes.NOTE_HAS_NOT_BEEN_CREATED));
            }
        } catch (DataIntegrityViolationException e){
            throw new NoteIntegrityException(NotesErrorCodes.NOTE_INTEGRITY_VIOLATION.getCode(), e.getMessage());
        }

        return note;
    }

    public List<Note> findNoteByOwner(UUID ownerId){
       return noteDao.findByOwner(ownerId);
    }

    public List<Note> findNoteByFolder(UUID folderId){
        return noteDao.findByFolder(folderId);
    }

    @Transactional
    public Note updateNote(Note note){
        checkCurrentUser(note.getOwnerColleagueUuid());
        checkReferenceColleague(note.getOwnerColleagueUuid(), note.getReferenceColleagueUuid());

        try {
            if (1 == noteDao.update(note)) {
                return note;
            }
        } catch (DataIntegrityViolationException e) {
            throw new NoteIntegrityException(NotesErrorCodes.NOTE_INTEGRITY_VIOLATION.getCode(), e.getMessage());
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

        try {
            if (0 == folderDao.create(folder)) {
                throw new UnknownDataManipulationException(messageSourceAccessor.getMessage(NotesErrorCodes.FOLDER_HAS_NOT_BEEN_CREATED));
            }
        } catch (DataIntegrityViolationException e) {
            throw new NoteIntegrityException(NotesErrorCodes.FOLDER_INTEGRITY_VIOLATION.getCode(), e.getMessage());
        }

        return folder;
    }

    public List<Folder> findFolderByOwner(UUID uuid){
        return folderDao.findByOwner(uuid);
    }

    @Transactional
    public Folder updateFolder(Folder folder){
        checkCurrentUser(folder.getOwnerColleagueUuid());

        try {
            if (1 == folderDao.update(folder)) {
                return folder;
            }
        } catch (DataIntegrityViolationException e) {
            throw new NoteIntegrityException(NotesErrorCodes.FOLDER_INTEGRITY_VIOLATION.getCode(), e.getMessage());
        }

        throw new NotFoundException(NotesErrorCodes.FOLDER_NOT_FOUND.getCode(),
                messageSourceAccessor.getMessage(NotesErrorCodes.FOLDER_NOT_FOUND, Map.of("id", folder.getId())));
    }

    @Transactional
    public void deleteFolder(UUID uuid){
        folderDao.delete(uuid);
    }

    private void checkCurrentUser(UUID colleagueUuid){
        if(!getCurrentUserUuid().equals(colleagueUuid)){
            throw new BadCredentialsException(messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_AUTHORIZED));
        }
    }

    private void checkReferenceColleague(UUID managerUuid, UUID referenceColleagueId){

        if(referenceColleagueId == null || managerUuid == null){
            return;
        }

        if(managerUuid.equals(referenceColleagueId)){
            throw new NoteIntegrityException(NotesErrorCodes.NOTE_OWNER_REFERENCE_COLLISION.getCode(),
                    messageSourceAccessor.getMessage(NotesErrorCodes.NOTE_OWNER_REFERENCE_COLLISION, Map.of("id", managerUuid)));
        }

        var referenceColleague = configEntryDAO.getColleague(referenceColleagueId);

        if(referenceColleague == null){
            throw new NotFoundException(ErrorCodes.USER_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_FOUND,
                            Map.of("param_name", "colleague ID", "param_value", referenceColleagueId.toString())));
        }

        if(!managerUuid.equals(referenceColleague.getManagerUuid())){
            throw new NoteIntegrityException(NotesErrorCodes.NOT_A_LINE_MANAGER.getCode(),
                    messageSourceAccessor.getMessage(NotesErrorCodes.NOT_A_LINE_MANAGER, Map.of("curId", managerUuid, "refId", referenceColleagueId)));
        }
    }

    private UUID getCurrentUserUuid(){

        var user = userService.currentUser(EnumSet.of(UserIncludes.SUBSIDIARY_PERMISSIONS)) //TODO includes?
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ErrorCodes.USER_NOT_FOUND,
                                Map.of("param_name", "Authentication", "param_value", ""))));

        return user.getColleagueUuid();
    }

}
