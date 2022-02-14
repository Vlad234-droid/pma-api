package com.tesco.pma.cms.dao;

import com.tesco.pma.cms.model.Content;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface ContentDAO {

    List<Content> findByKey(@Param("key") String key);

    Content findById(@Param("uuid") UUID uuid);

    List<Content> find(@Param("requestQuery") RequestQuery requestQuery);

    int create(@Param("content") Content content);

    int delete(@Param("id") UUID uuid);

    int update(@Param("content") Content content);

}
