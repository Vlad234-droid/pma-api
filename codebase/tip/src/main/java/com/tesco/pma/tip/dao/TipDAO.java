package com.tesco.pma.tip.dao;

import com.tesco.pma.tip.api.Tip;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TipDAO {

    int insert(@Param("tip") Tip tip);

    List<Tip> selectAll();

    Tip selectByUuid(@Param("uuid") UUID uuid);

    int update(@Param("tip") Tip tip);

    int delete(@Param("uuid") UUID uuid);

    int publish(@Param("uuid") UUID uuid, @Param("history") Timestamp... history);
}
