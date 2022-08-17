package com.github.everything;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author wangquan07
 * 2021/12/8 11:56
 */
@Slf4j
public class ThreadPoolExecutorExt {
    private ThreadPoolExecutor threadPoolExecutor;

    ThreadPoolExecutorExt(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public Future<?> submit(Runnable task) {
        return threadPoolExecutor.submit(task);
    }

    public void switchThreadNum(int threadNum) {
        threadPoolExecutor.setMaximumPoolSize(threadNum);
        threadPoolExecutor.setCorePoolSize(threadNum);
//        ThreadPoolExecutor threadPoolExecutorNew = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
//        ThreadPoolExecutor old = threadPoolExecutor;
//        // 切换为新线程池
//        threadPoolExecutor = threadPoolExecutorNew;
//        // 老线程池，只剩下执行中以及队列中任务
//        old.shutdown();
//
//        try {
//            int num = 1;
//            while (true) {
//                boolean shutdown = old.awaitTermination(10, TimeUnit.MILLISECONDS);
//                if (shutdown) {
//                    // 线程池不再有任务
//                    log.info("线程池回收成功");
//                    break;
//                }  else {
//                    log.info("等待回收失败" + num++);
//                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1));
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                // 无界队列+0核心线程, 也可以执行该行
                System.out.println("1");
            }
        });
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                // 无界队列+0核心线程, 也可以执行该行
                System.out.println("1");
            }
        });
        System.out.println();
    }

}
