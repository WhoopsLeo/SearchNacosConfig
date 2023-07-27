package com.colipu.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    //核心线程数
    private static final int CORE_POOL_SIZE = 20;

    //最大可创建的线程数
    private static final int MAX_POOL_SIZE = 30;

    //阻塞队列的长度
    private static final int QUEUE_CAPACITY = 2000;

    //线程池维护线程所允许的空闲时间
    private static final int KEEP_ALIVE_SECONDS = 1000;

    @Bean("taskExecutor")
    public ExecutorService executorService() {
        LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(QUEUE_CAPACITY);

        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.MILLISECONDS,
                queue,
                new ThreadPoolExecutor.AbortPolicy());
    }


}
