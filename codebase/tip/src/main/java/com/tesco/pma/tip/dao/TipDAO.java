package com.tesco.pma.tip.dao;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface TipDAO {

    int create(@Param("tip") Tip tip);

    List<Tip> findByRequestQuery(@Param("requestQuery") RequestQuery requestQuery);

    Tip findByUuid(@Param("uuid") UUID uuid);

    int delete(@Param("uuid") UUID uuid);
}
