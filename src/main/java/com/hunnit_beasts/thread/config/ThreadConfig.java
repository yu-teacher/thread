package com.hunnit_beasts.thread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;

@Configuration
public class ThreadConfig implements AsyncConfigurer {

    @Bean(name = "virtualThreadExecutor")
    public AsyncTaskExecutor virtualThreadExecutor() {
        // 가상 스레드 구성
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean(name = "platformThreadExecutor")
    public ThreadPoolTaskExecutor platformThreadExecutor() {
        // 플랫폼 스레드 풀 구성
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("platform-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncTaskExecutor getAsyncExecutor() {
        // 기본 비동기 실행자로 가상 스레드 사용
        return virtualThreadExecutor();
    }
}