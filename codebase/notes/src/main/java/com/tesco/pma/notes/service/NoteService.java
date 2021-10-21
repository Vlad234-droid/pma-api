package com.tesco.pma.notes.service;

import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.dao.IFolderDao;
import com.tesco.pma.notes.dao.INoteDao;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.service.user.UserIncludes;
import com.tesco.pma.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class NoteService {

    private final IFolderDao folderDao;
    private final INoteDao noteDao;
    private final UserService userService;

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

    @Transactional
    public List<Note> findNoteByOwnerColleagueUuid(UUID uuid){
       return noteDao.findByOwnerColleagueUuid(uuid);
    }

    @Transactional
    public Note updateNote(Note note){
        if (1 == noteDao.update(note)) {
            return note;
        }
        throw new NotFoundException(HttpStatusCodes.NOT_FOUND, "Note "+ note.getId().toString()+" not found");
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

    @Transactional
    public List<Folder> findFolderByOwnerColleagueUuid(UUID uuid){
        return folderDao.findByOwnerColleagueUuid(uuid);
    }

    @Transactional
    public Folder updateFolder(Folder folder){
        if (1 == folderDao.update(folder)) {
            return folder;
        }
        throw new NotFoundException(HttpStatusCodes.NOT_FOUND, "Note "+ folder.getId().toString()+" not found");
    }

    @Transactional
    public void deleteFolder(UUID uuid){
        folderDao.delete(uuid);
    }

    private void checkCurrentUser(UUID colleagueUuid){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = userService.findUserByAuthentication(authentication, EnumSet.of(UserIncludes.SUBSIDIARY_PERMISSIONS)) //TODO includes?
                .orElseThrow(()->new NotFoundException(HttpStatusCodes.BAD_REQUEST ,"Current user not found"));

        if(!user.getColleagueUuid().equals(colleagueUuid)){
            //TODO find/create appropriate exception
            throw new RuntimeException("Given colleague uuid is mismatched with the current user's uuid");
        }
    }

}
