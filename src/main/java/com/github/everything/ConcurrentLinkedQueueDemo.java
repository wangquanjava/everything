package com.github.everything;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author wangquan07
 * 2022/3/16 18:05
 */
public class ConcurrentLinkedQueueDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

        concurrentLinkedQueue.add(11);
        Integer poll = concurrentLinkedQueue.poll();
    }
}
