package com.wang.bi.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor(){
        //创建一个线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            //初始化线程数为1
            private int count = 1;
            //每当线程池需要创建新线程时，就会调用newThread方法
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("my-thread-pool-" + count);
                count++;
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 120L, TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),threadFactory);
        return threadPoolExecutor;
    }
}
