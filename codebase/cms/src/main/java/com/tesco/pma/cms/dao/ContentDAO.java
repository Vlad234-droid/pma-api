package com.tesco.pma.cms.dao;

import com.tesco.pma.cms.model.Content;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface ContentDAO {

    List<Content> findByKey(@Param("key") String key);

    int create(@Param("content") Content content);

    int delete(@Param("id") UUID uuid);

}
