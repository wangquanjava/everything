package com.github.everything.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author wangquan07
 * 2022/4/29 16:19
 */
@Service
@Slf4j
public class RedisService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${delay.auto.consumer}")
    private boolean autoConsumer;

    @PostConstruct
    public void consumer() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (autoConsumer) {
                    consumerLessUnixTime(System.currentTimeMillis() / 1000);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void addRelayData(long unixTime, String data) {
        String unixTimeStr = String.valueOf(unixTime);
        // 追加到以时间戳为key的list右侧
        Long aLong = stringRedisTemplate.opsForList().rightPush(unixTimeStr, data);

        // 确保zset中有该元素
        Boolean delayqueue = stringRedisTemplate.opsForZSet().add("delayqueue", unixTimeStr, unixTime);
    }

    public List<String> consumerLessUnixTime(long unixTime) {
        // 查询zset，并对查询出的zset进行删除
        List<Object> objects = stringRedisTemplate.executePipelined(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.zSetCommands().zRangeByScoreWithScores("delayqueue".getBytes(), 0, unixTime);
                connection.zSetCommands().zRemRangeByScore("delayqueue".getBytes(), 0, unixTime);
                return null;
            }
        });
        // 判空
        Object o = objects.get(0);
        if (!(o instanceof LinkedHashSet)) {
            log.info("zset没有找到<{}的数据", unixTime);
            return Collections.emptyList();
        }

        // 拿到事件戳list
        LinkedHashSet<DefaultTypedTuple<String>> linkedHashSet = (LinkedHashSet) objects.get(0);
        List<String> result = Lists.newArrayList();
        linkedHashSet.forEach(stringDefaultTypedTuple -> result.add(stringDefaultTypedTuple.getValue()));
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyList();
        }


        // 事件戳为key循环查list
        ArrayList<String> dataList = Lists.newArrayList();
        for (String string : result) {
            while(true) {
                String s = stringRedisTemplate.opsForList().leftPop(string);
                if (s == null) {
                    break;
                }
                dataList.add(s);
                log.info("收到一个延时消息，事件戳:{}, 数据:{}", string, s);
            }
        }
        return dataList;
    }
}
