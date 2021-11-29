package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.dao.PMColleagueCycleDAO;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class PMColleagueCycleServiceImpl implements PMColleagueCycleService {

    private final BatchService batchService;
    private final PMColleagueCycleDAO dao;

    @Override
    public void saveColleagueCycles(Collection<PMColleagueCycle> colleagueCycles) {
        batchService.executeDBOperationInBatch(colleagueCycles, dao::create);
    }
}
