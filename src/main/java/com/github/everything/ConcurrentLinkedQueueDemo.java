package com.github.everything;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author wangquan07
 * 2022/3/16 18:05
 */
public class ConcurrentLinkedQueueDemo {
    public static void main(String[] args) {
        ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

        concurrentLinkedQueue.add(11);
        Integer poll = concurrentLinkedQueue.poll();
    }
}
