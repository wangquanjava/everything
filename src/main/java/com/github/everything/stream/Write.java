package com.github.everything.stream;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Write {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private int maxSize = 10;
    private int threadCount = 10;
    private ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

    private AtomicInteger integer;
    @PostConstruct
    private void init() {
        integer = new AtomicInteger(Integer.valueOf(stringRedisTemplate.opsForValue().get("queue_index")));
        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000000; i++) {
                        while (true) {
                            // 无限重试写入i
                            int integerValue = integer.intValue();
                            Long aLong = stringRedisTemplate.opsForList().rightPush(String.valueOf(integerValue), Thread.currentThread().getId() + "_" + i);
                            if (aLong > 10L) {
                                integer.weakCompareAndSet(integerValue, integerValue + 1);
                            } else if (aLong == 1) {
                                stringRedisTemplate.opsForValue().set("queue_index", String.valueOf(integerValue));
                                break;
                            } else {
                                break;
                            }
                        }
                        try {
                            Thread.sleep(RandomUtils.nextInt(1000, 2000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

}
