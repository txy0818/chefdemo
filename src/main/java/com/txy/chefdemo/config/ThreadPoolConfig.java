package com.txy.chefdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Configuration
public class ThreadPoolConfig {
    @Bean("orderCancelExecutor")
    public Executor orderCancelExecutor() {
        return new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadPoolExecutor.DiscardPolicy());
    }
}
