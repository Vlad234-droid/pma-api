package com.tesco.pma.process.dao;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 22:40
 */
class PMProcessDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/process/dao/";
    private static final UUID PM_UUID = UUID.fromString("4f2ab073-2c31-11ec-916b-0242391d2e7a");
    private static final UUID CL_UUID = UUID.fromString("cf2ab073-2c31-11ec-916b-0242391d2e7c");

    @Autowired
    private PMProcessDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void create() {

    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_process_init.xml"})
    void read() {
        var process = dao.read(PM_UUID);
        Assertions.assertNotNull(process);
        var hi = dao.readHistory(PM_UUID);
        Assertions.assertNotNull(hi);
        Assertions.assertEquals(1, hi.size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_process_init.xml"})
    void updateStatus() {

    }
}