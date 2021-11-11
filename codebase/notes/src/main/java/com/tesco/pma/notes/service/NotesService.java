package com.tesco.pma.notes.service;

import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;

import java.util.List;
import java.util.UUID;

public interface NotesService {

    Note createNote(Note note);

    List<Note> findNoteByOwner(UUID ownerId);

    List<Note> findNoteByFolder(UUID folderId);

    Note updateNote(Note note);

    void deleteNote(UUID uuid);

    Folder createFolder(Folder folder);

    List<Folder> findFolderByOwner(UUID uuid);

    Folder updateFolder(Folder folder);

    void deleteFolder(UUID uuid);

}
