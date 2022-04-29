package com.github.everything;

import com.github.everything.service.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author wangquan07
 * 2022/4/29 16:49
 */
@RestController
public class DelayController {
    @Autowired
    private RedisService redisService;

    @GetMapping("/addDelayData")
    public void addDelayData(String time) {
        long second;
        if (StringUtils.isNotBlank(time)) {
            second = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")).toEpochSecond(ZoneOffset.of("+8"));
        } else {
            second = System.currentTimeMillis() / 1000;
        }
        redisService.addRelayData(second, DateFormatUtils.format(second * 1000, "yyyyMMdd HH:mm:ss"));
    }
    @GetMapping("/consumerData")
    public List<String> consumerData() {
        long unixTime = System.currentTimeMillis() / 1000;
        return redisService.consumerLessUnixTime(unixTime);
    }
}
