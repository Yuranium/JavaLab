package com.javalab.executionservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncExecutionConfig
{
    @Value("${code-execution.async.core-pool-size}")
    private int corePoolSize;

    @Value("${code-execution.async.pool-size}")
    private int maxPoolSize;

    @Value("${code-execution.async.queue-capacity}")
    private int queueCapacity;

    @Value("${code-execution.async.thread-name-prefix:execution-}")
    private String threadNamePrefix;

    @Bean("codeExecutionExecutor")
    public Executor codeExecutionExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}