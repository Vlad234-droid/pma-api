package com.tesco.pma.dao;

import com.tesco.pma.api.Subsidiary;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on subsidiary
 */
public interface SubsidiaryDAO {

    /**
     * Returns a subsidiary
     *
     * @param subsidiaryUuid an identifier
     * @return a Subsidiary
     */
    Subsidiary get(@Param("subsidiaryUuid") UUID subsidiaryUuid);

    /**
     * Returns all subsidiaries
     *
     * @return a list of Subsidiaries
     */
    List<Subsidiary> getAll();


    /**
     * Inserts a subsidiary
     *
     * @param subsidiary a Subsidiary
     * @return number of inserted subsidiaries
     */
    int insert(@Param("subsidiary") Subsidiary subsidiary);

    /**
     * Update a subsidiary
     *
     * @param subsidiary a Subsidiary
     * @return number of updated subsidiaries
     */
    int update(@Param("subsidiary") Subsidiary subsidiary);

    /**
     * Deletes a subsidiary
     *
     * @param subsidiaryUuid an identifier
     * @return number of deleted subsidiaries
     */
    int deleteSubsidiary(@Param("subsidiaryUuid") UUID subsidiaryUuid);
}
