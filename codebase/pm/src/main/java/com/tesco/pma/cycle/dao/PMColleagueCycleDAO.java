package com.tesco.pma.cycle.dao;

import com.tesco.pma.cycle.api.PMColleagueCycle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PMColleagueCycleDAO {

    int create(@Param("cc") List<PMColleagueCycle> pmColleagueCycle);

}
