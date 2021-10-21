package com.tesco.pma.process.dao;

import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.TimelineResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface PMRuntimeProcessMetadataDAO {


    int saveProcessMetadata(@Param("processUuid") UUID processUuid,
                     @Param("metadata") PMProcessMetadata metadata);

   List<TimelineResponse> readMetadata(@Param("processUuid") UUID processUuid);
}
