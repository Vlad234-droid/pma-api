package com.tesco.pma.notes.dao;

import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface INoteDao {

    int create(@Param("note") Note note);

    List<Note> findByOwnerColleagueUuid(@Param("colleagueUuid") UUID colleagueUuid);

    int update(@Param("note") Note note);

    int delete(@Param("id") UUID uuid);

}
