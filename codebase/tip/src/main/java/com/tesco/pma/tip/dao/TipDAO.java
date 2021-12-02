package com.tesco.pma.tip.dao;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TipDAO {

    int create(@Param("tip") Tip tip);

    List<Tip> findAll(@Param("requestQuery") RequestQuery requestQuery);

    Tip read(@Param("key") String key);

    List<Tip> findHistory(@Param("key") String key);

    int delete(@Param("key") String key);
}
