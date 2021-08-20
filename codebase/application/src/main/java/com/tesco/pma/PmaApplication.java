package com.tesco.pma;

import com.tesco.pma.configuration.AsyncConfig;
import com.tesco.pma.dao.config.MybatisDefaultConfig;
import com.tesco.pma.logging.configuration.RequestLoggerConfiguration;
import com.tesco.pma.profile.configuration.ProfileModuleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * The PMA application.
 */
@SpringBootApplication(scanBasePackages = {"com.tesco.pma"})
@Import({
        MybatisDefaultConfig.class,
        RequestLoggerConfiguration.class,
        AsyncConfig.class,
        ProfileModuleConfig.class
})
public class PmaApplication { // NOPMD

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Runs Spring Boot Module.
     *
     * @param args parameters
     */

    public static void main(String[] args) {
        SpringApplication.run(PmaApplication.class, args);
    }
}
