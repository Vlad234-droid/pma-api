package com.tesco.pma.cms.dao;

import com.tesco.pma.cms.model.ContentEntry;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface ContentEntryDAO {

    List<ContentEntry> find(@Param("requestQuery") RequestQuery requestQuery);

    int create(@Param("content") ContentEntry contentEntry);

    int delete(@Param("uuid") UUID uuid);

    int update(@Param("content") ContentEntry contentEntry);

}
