package com.tesco.pma.cms.dao;

import com.tesco.pma.cms.model.Content;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContentDAO {

    List<Content> findByKey(@Param("key") String key);

    int create(@Param("content") Content content);

}
