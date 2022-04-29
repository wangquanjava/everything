package com.github.everything;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author wangquan07
 * 2022/4/24 18:03
 */
@Slf4j
public class ThreadlocalThreadPool {
    public static final ThreadLocal<Boolean> threadLocal = new InheritableThreadLocal<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        threadLocal.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("子线程" + threadLocal.get());
            }
        }).start();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                log.info("子线程：" + threadLocal.get());
                threadLocal.remove();
            }
        });
        Future<?> submit = threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                log.info("子线程：" + threadLocal.get());
                threadLocal.remove();
            }
        });
        submit.get();
        log.info("main线程：" + threadLocal.get());
    }
}
