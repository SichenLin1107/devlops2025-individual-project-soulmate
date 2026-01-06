package com.soulmate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 * 配置异步任务执行器和线程池
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Value("${async.core-pool-size:2}")
    private int corePoolSize;
    
    @Value("${async.max-pool-size:5}")
    private int maxPoolSize;
    
    @Value("${async.queue-capacity:100}")
    private int queueCapacity;
    
    @Value("${async.thread-name-prefix:async-}")
    private String threadNamePrefix;
    
    @Value("${async.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    /**
     * 异步任务执行器 - 用于文档处理等异步任务
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程名前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // 拒绝策略：当任务添加到线程池中被拒绝时，使用调用者所在的线程来执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
}
