package com.github.everything;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author wangquan07
 * 2021/11/30 11:46
 */
@Slf4j
@RestController
public class Quake {
    private Long sleepMs = null;

    @Autowired
    private Task task;

    @GetMapping("press")
    public int press(int qps) {
        sleepMs = (long) 1000 / qps;
        return qps;
    }

    @GetMapping("threadPool")
    public int threadPool(int threadNum) {
        Task.threadPoolExecutor.switchThreadNum(threadNum);
        return threadNum;
    }

    @PostConstruct
    public void request() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (sleepMs == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (sleepMs == null) {
                        continue;
                    }
                    task();
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }).start();

    }

    /**
     * 压测内容都可以放到这里
     */
    public void task() {

        try {
            task.task(System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            log.error("提交任务失败");
        }
    }

}
