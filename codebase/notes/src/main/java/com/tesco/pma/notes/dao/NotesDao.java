package com.tesco.pma.notes.dao;

import com.tesco.pma.notes.model.Note;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.UUID;

public interface NotesDao {

    int create(@Param("note") Note note);

    List<Note> findByOwner(@Param("colleagueUuid") UUID colleagueUuid);

    List<Note> findByFolder(@Param("folderUuid") UUID folderUuid);

    int update(@Param("note") Note note);

    int delete(@Param("id") UUID uuid);

}
