package com.github.everything;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangquan07
 * 2022/4/24 18:03
 */
public class ThreadlocalThreadPool {
    public static final ThreadLocal<Boolean> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {

        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
        threadLocal.set(true);
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程：" + threadLocal.get());
            }
        });

    }
}
