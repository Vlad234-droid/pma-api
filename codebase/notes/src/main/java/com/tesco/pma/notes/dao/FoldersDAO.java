package com.tesco.pma.notes.dao;

import com.tesco.pma.notes.model.Folder;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface FoldersDAO {

    int create(@Param("folder") Folder folder);

    List<Folder> findByOwner(@Param("colleagueUuid") UUID colleagueUuid);

    int update(@Param("folder") Folder folder);

    int delete(@Param("id") UUID uuid);

}
