package com.github.everything.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangquan07
 * 2022/3/9 10:47
 */
@Slf4j
public class GuavaDemo {
    public static void main(String[] args) {
        Cache<Integer, Integer> build = CacheBuilder.newBuilder().concurrencyLevel(16).maximumSize(3000).recordStats().removalListener(new RemovalListener<Integer, Integer>() {
            @Override
            public void onRemoval(RemovalNotification<Integer, Integer> notification) {
                log.error("remove监听,key:{},原因:{}", notification.getKey(), notification.getCause());
            }
        }).build();

        for (int i = 0; i < 100; i++) {
            build.put(i, i);
            System.out.println(build.size());
        }
    }
}