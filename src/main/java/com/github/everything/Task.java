package com.github.everything;

import org.springframework.stereotype.Service;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author wangquan07
 * 2021/11/29 00:25
 */
@Service
public class Task {
    private static final int consumerThreadCount = 2;
    public static final ThreadPoolExecutorExt threadPoolExecutor = new ThreadPoolExecutorExt(consumerThreadCount, consumerThreadCount, 0, TimeUnit.SECONDS, new SynchronousQueue<>());

    public void task(long num) {
        // 队列中一直加数据, 可以理解成队列中一直有数据
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + "输出" + num);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
