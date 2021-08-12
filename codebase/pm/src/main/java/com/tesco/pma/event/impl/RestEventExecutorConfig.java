package com.tesco.pma.event.impl;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class RestEventExecutorConfig implements AsyncConfigurer {

    @Value("${tesco.application.rest-event-executor.core-pool-size:1}")
    private int corePoolSize;

    @Value("${tesco.application.rest-event-executor.max-core-pool-size:3}")
    private int maxCorePoolSize;

    @Value("${tesco.application.rest-event-executor.queue-capacity:100000}")
    private int queueCapacity;

    @Override
    @Bean(name = "restEventExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxCorePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("RestEventExecutor-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
