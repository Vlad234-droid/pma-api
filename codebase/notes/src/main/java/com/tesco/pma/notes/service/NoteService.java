package com.tesco.pma.notes.service;

import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.dao.IFolderDao;
import com.tesco.pma.notes.dao.INoteDao;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.rest.HttpStatusCodes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class NoteService {

    private final IFolderDao folderDao;
    private final INoteDao noteDao;

    @Transactional
    public void createNote(Note note){
        log.debug("Creating a Note {} for {}", note.getTitle(), note.getOwnerColleagueUuid().toString());
        //TODO check if current user is the owner of the note being created
        //TODO check if the current user is a line manager of the reference colleague (if presented)

        try {
            if (1 != noteDao.create(note)) {
                throw new RuntimeException("Note " + note.getId() + " hasn't been created");
            }
        } catch (DuplicateKeyException e){
            throw new AlreadyExistsException(HttpStatusCodes.BAD_REQUEST, "Note " + note.getId() + " already exists");
        }
    }

    @Transactional
    public List<Note> findNoteByOwnerColleagueUuid(UUID uuid){
       return noteDao.findByOwnerColleagueUuid(uuid);
    }

    @Transactional
    public int updateNote(Note note){
        if (1 == noteDao.update(note)) {
            return 1;
        }
        throw new NotFoundException(HttpStatusCodes.NOT_FOUND, "Note "+ note.getId().toString()+" not found");
    }

    @Transactional
    public int deleteNote(UUID uuid){
        return noteDao.delete(uuid);
    }

    @Transactional
    public void createFolder(Folder folder){
        log.debug("Creating folder {} for {}", folder.getTitle(), folder.getOwnerColleagueUuid().toString());
        //TODO check if current user is the owner of the folder being created
        folderDao.create(folder);
    }

    @Transactional
    public List<Folder> findFolderByOwnerColleagueUuid(UUID uuid){
        return folderDao.findByOwnerColleagueUuid(uuid);
    }

    @Transactional
    public int updateFolder(Folder folder){
        return folderDao.update(folder);
    }

    @Transactional
    public int deleteFolder(UUID uuid){
        return folderDao.delete(uuid);
    }

}
