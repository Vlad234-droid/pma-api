package com.tesco.pma.configuration;

import com.tesco.pma.exception.LimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${spring.async-executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${spring.async-executor.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${spring.async-executor.queue-capacity:10}")
    private int queueCapacity;

    @Bean(name = "processingTaskExecutor")
    public Executor processingTaskExecutor() {
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix("PMAProcessingAsync-");
        taskExecutor.setRejectedExecutionHandler((runnable, threadPoolExecutor) -> {
            throw new LimitExceededException();
        });
        taskExecutor.initialize();
        return new DelegatingSecurityContextTaskExecutor(taskExecutor);
    }
}
