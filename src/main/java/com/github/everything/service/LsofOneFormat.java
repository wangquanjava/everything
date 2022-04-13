package com.github.everything.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对lsof输出分析
 *
 * @author wangquan07
 * 2022/4/2 22:16
 */
@Slf4j
public class LsofOneFormat {
    private List<Integer> serverPortList = Lists.newArrayList(8011);
    private Map<String, String> appkeyByHostKey = Maps.newHashMap();

    public static void main(String[] args) {
        // 共有多少行、input多少行、output多少行
        LsofOneFormat lsofOneFormat = new LsofOneFormat();
        List<LsofLine> data1 = lsofOneFormat.file2List("/Users/wangquan07/Downloads/cbaseAll2258.log");
        List<LsofLine> data2 = lsofOneFormat.file2List("/Users/wangquan07/Downloads/cbaseAll2324.log");
        log.info("波动前连接:{}", lsofOneFormat.printSummary(data1));
        log.info("波动后连接:{}", lsofOneFormat.printSummary(data2));

        List<LsofLine> diffClose = lsofOneFormat.getDiff(data1, data2);
        log.info("被回收连接:{}", lsofOneFormat.printSummary(diffClose));

        List<LsofLine> diffCreate = lsofOneFormat.getDiff(data2, data1);
        log.info("重新创建连接:{}", lsofOneFormat.printSummary(diffCreate));

        List<LsofLineGroupBy> serverGroupByAppKey = lsofOneFormat.groupByAppKey(lsofOneFormat.getServerList(diffCreate));
        log.info("重新创建连接中server连接占比分析:\n{}", lsofOneFormat.printDiffSummaryByAppKey(serverGroupByAppKey, lsofOneFormat.getServerList(data2)));

        List<LsofLineGroupBy> clientGroupByAppKey = lsofOneFormat.groupByAppKey(lsofOneFormat.getClientList(diffCreate));
        log.info("重新创建连接中client连接占比分析:\n{}", lsofOneFormat.printDiffSummaryByAppKey(clientGroupByAppKey, lsofOneFormat.getClientList(data2)));

    }

    public String printSummary(List<LsofLine> data) {
        return String.format("共%s行，其中server:%s、client:%s", data.size(), getServerList(data).size(), getClientList(data).size());
    }

    public String printDiffSummaryByAppKey(List<LsofLineGroupBy> data, List<LsofLine> now) {
        List<LsofLineGroupBy> groupByAppKey = groupByAppKey(now);
        Map<String, LsofLineGroupBy> nowMap = groupByAppKey.stream().collect(Collectors.toMap(LsofLineGroupBy::getKey, a -> a));
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder stringBuilder = new StringBuilder();
        String format = "%60s : %11s : %s\n";
        stringBuilder.append(String.format(format, "appkey", "createCount", "countCount/all"));
        for (LsofLineGroupBy datum : data) {
            stringBuilder.append(String.format(format, datum.getKey(), datum.getCount(), decimalFormat.format(datum.getCount() * 1.0 / nowMap.get(datum.getKey()).getCount())));
        }
        return stringBuilder.toString();
    }

    public String printDiffSummaryByRemoteHost(List<LsofLineGroupBy> data, List<LsofLine> now) {
        List<LsofLineGroupBy> groupByAppKey = groupByRemoteHost(now);
        Map<String, LsofLineGroupBy> nowMap = groupByAppKey.stream().collect(Collectors.toMap(LsofLineGroupBy::getKey, a -> a));
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder stringBuilder = new StringBuilder();
        String format = "%30s : %11s : %s\n";
        stringBuilder.append(String.format(format, "appkey", "createCount", "countCount/all"));
        for (LsofLineGroupBy datum : data) {
            stringBuilder.append(String.format(format, datum.getKey(), datum.getCount(), decimalFormat.format(datum.getCount() * 1.0 / nowMap.get(datum.getKey()).getCount())));
        }
        return stringBuilder.toString();
    }


    public List<LsofLine> getDiff(List<LsofLine> data1, List<LsofLine> data2) {
        List<Long> data1DeviceId = data1.stream().map(LsofLine::getDeviceId).collect(Collectors.toList());
        List<Long> data2DeviceId = data2.stream().map(LsofLine::getDeviceId).collect(Collectors.toList());
        List<Long> subtract = ListUtils.subtract(data1DeviceId, data2DeviceId);
        Map<Long, LsofLine> map = data1.stream().collect(Collectors.toMap(LsofLine::getDeviceId, a -> a));
        return subtract.stream().map(a -> map.get(a)).collect(Collectors.toList());
    }

    public List<LsofLine> getServerList(List<LsofLine> data) {
        List<LsofLine> result = Lists.newArrayListWithCapacity(data.size());
        for (LsofLine datum : data) {
            if (serverPortList.contains(datum.getLocalPort())) {
                result.add(datum);
            }
        }
        return result;
    }

    public List<LsofLine> getClientList(List<LsofLine> data) {
        List<LsofLine> result = Lists.newArrayListWithCapacity(data.size());
        for (LsofLine datum : data) {
            if (!serverPortList.contains(datum.getLocalPort())) {
                result.add(datum);
            }
        }
        return result;
    }

    public List<LsofLine> file2List(String path) {
        List<String> list = null;
        try {
            list = FileUtils.readLines(new File(path), Charset.defaultCharset());
        } catch (IOException e) {
            log.error("加载文件异常", e);
            return Collections.emptyList();
        }

        List<LsofLine> result = Lists.newArrayList();
        int errorCount = 0;
        for (String s : list) {
            List<String> strings = Splitter.on(" ").splitToStream(s).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (!StringUtils.contains(s, "TCP")) {
                continue;
            }
            if (CollectionUtils.size(strings) != 10) {
                continue;
            }

            // 示例：[java, 263020, sankuai, 1343u, IPv4, 2111011513, 0t0, TCP, set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922, (ESTABLISHED)]
            try {
                String name = strings.get(8);
                LsofLine lsofLine = new LsofLine()
                        .setDeviceId(Long.parseLong(strings.get(5)))
                        .setName(name)
                        .setLocalPort(getLocalPort(name))
                        .setRemoveHost(getRemoteHost(name))
                        .setRemoteAppkey(getRemoteAppkey(name));
                result.add(lsofLine);
            } catch (Exception e) {
                errorCount++;
                continue;
            }
        }
        log.info("转换完成，失败次数:{}", errorCount);
        return result;
    }


    private String getAppekeyFromOps(String host) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URIBuilder(String.format("http://ops.vip.sankuai.com/api/v0.2/hosts/%s/appkeys", host)).build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Cookie", "_lxsdk_cuid=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; _lxsdk=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; s_u_745896=wPzGrzS+8NIHAQMhaT8k6w==; al=etbrsabkzqwplazomicpeoybunodsbqz; u=7113786; uu=bf70caf0-b7fa-11ec-bdc2-1d9e2782e776; cid=1; s_m_id_3299326472=AwMAAAA5AgAAAAIAAAE9AAAALPVKSfqvDQPyb+RkNGaBXjnu3be7dnPmweuSYJAvu9s467ya2BSXoXF2+ZZeAAAAIyB6tbbO9Z0Rxc4NVeikjWfUkHFIJONN9h3mJpd+QTMhZAhA; csrftoken=Df9yzbAwHt1Pm4uxI2Gt95H0bkyRRsZs; sessionid=gjw22iae60b07374l9kud9tg4lxe12qx; ssoid=eAGFzjtLw1AYgGEOSCmdJJNjxjYQ_M4t5ztOpmnA0csguEjOJaP-AQebwVIQBDedFAIKgpdBcXERB3eXrg7V_AFHQUWc3R9e3jaZfXwdk_Bg8nRdAes4LRKEojSwEDqFCElJpWFCKMW1lj6yuqDJt6JO9Sck6K17s2b9lt_JMgop8jzLBcg07SMAQzqQmRB8kOYyvP3Yf96DLmH_hvFnabG1VE_PRxUsP9y9jD7nKxJ1Wiur2bbzQTCtz5qb07fxxfvJsLmsm6vh3Ey4e3_U6_7iQ9L-GzsmTDONHAsTq8SoWAhnYyxLiKk3VArnC0ndJk0ECoUUGQO5ETpOS6aU4RAZwSwaaZxXwLx0WoO1X2-2ZUQ**eAEFwYEBACAEBMCV5FHG8dT-I3S3zyXkqnPEewWBxESsjs45zcBLU5EXlzo0Ldq2XE5U4X04FBGm; _lxsdk_s=18020cef685-62f-327-eec%7C%7C352; _lxsdk_cuid=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; _lxsdk=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; s_u_745896=wPzGrzS+8NIHAQMhaT8k6w==; al=etbrsabkzqwplazomicpeoybunodsbqz; u=7113786; uu=bf70caf0-b7fa-11ec-bdc2-1d9e2782e776; cid=1; s_m_id_3299326472=AwMAAAA5AgAAAAIAAAE9AAAALPVKSfqvDQPyb+RkNGaBXjnu3be7dnPmweuSYJAvu9s467ya2BSXoXF2+ZZeAAAAIyB6tbbO9Z0Rxc4NVeikjWfUkHFIJONN9h3mJpd+QTMhZAhA; csrftoken=Df9yzbAwHt1Pm4uxI2Gt95H0bkyRRsZs; sessionid=gjw22iae60b07374l9kud9tg4lxe12qx; ssoid=eAGFzjtLw1AYgGEOSCmdJJNjxjYQ_M4t5ztOpmnA0csguEjOJaP-AQebwVIQBDedFAIKgpdBcXERB3eXrg7V_AFHQUWc3R9e3jaZfXwdk_Bg8nRdAes4LRKEojSwEDqFCElJpWFCKMW1lj6yuqDJt6JO9Sck6K17s2b9lt_JMgop8jzLBcg07SMAQzqQmRB8kOYyvP3Yf96DLmH_hvFnabG1VE_PRxUsP9y9jD7nKxJ1Wiur2bbzQTCtz5qb07fxxfvJsLmsm6vh3Ey4e3_U6_7iQ9L-GzsmTDONHAsTq8SoWAhnYyxLiKk3VArnC0ndJk0ECoUUGQO5ETpOS6aU4RAZwSwaaZxXwLx0WoO1X2-2ZUQ**eAEFwYEBACAEBMCV5FHG8dT-I3S3zyXkqnPEewWBxESsjs45zcBLU5EXlzo0Ldq2XE5U4X04FBGm; _lxsdk_s=18020cef685-62f-327-eec%7C%7C438");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                // 如果http状态码是200, 则取出响应体, 序列化为string
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity, "utf-8");
                JSONObject jsonObject = JSON.parseObject(content);
                JSONArray appkeys = jsonObject.getJSONArray("appkeys");
                return appkeys.get(0).toString();
            }
        } catch (Exception e) {
            log.error("请求ops获取appkeys异常", e);
        }
        return "";
    }

    //

    /**
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     */
    private String getRemoteAppkey(String name) {
        String host = getRemoteHost(name);
        String hostKey = getHostKey(name);
        String appkey = appkeyByHostKey.get(hostKey);
        if (appkey != null) {
            return appkey;
        }
        appkey = getAppekeyFromOps(host);
        if (StringUtils.isNotBlank(appkey)) {
            appkeyByHostKey.put(hostKey, appkey);
            return appkey;
        }
        return hostKey;
    }

    private String getHostKey(String name) {
        List<String> split = Splitter.on("->").splitToList(name);
        name = split.get(1);
        List<String> split1 = Splitter.on(".").splitToList(name);
        name = split1.get(0);
        name = StringUtils.removePattern(name, "[0-9]+");

        String[] split2 = StringUtils.split(name, "-");
        // api-managerserver
        return Joiner.on("-").join(split2[split2.length - 2], split2[split2.length - 1]);
    }

    /**
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     * @return set-zf-order-api-managerserver53.mt
     */
    public String getRemoteHost(String name) {
        List<String> split = Splitter.on("->").splitToList(name);
        name = split.get(1);
        List<String> split1 = Splitter.on(":").splitToList(name);
        return split1.get(0);
    }

    /**
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     * @return 8011
     */
    public int getLocalPort(String name) {
        List<String> split = Splitter.on("->").splitToList(name);
        name = split.get(0);
        List<String> split1 = Splitter.on(":").splitToList(name);
        return Integer.parseInt(split1.get(1));
    }

    private List<LsofLineGroupBy> groupByAppKey(List<LsofLine> lsofLinesPre) {
        List<LsofLineGroupBy> objects = Lists.newArrayList();
        HashMap<String, Integer> countMap = Maps.newHashMap();
        for (LsofLine lsofLine : lsofLinesPre) {
            String appkey = lsofLine.getRemoteAppkey();
            Integer integer = countMap.get(appkey);
            if (integer == null) {
                countMap.put(appkey, 1);
            } else {
                countMap.put(appkey, integer + 1);
            }
        }

        for (Map.Entry<String, Integer> entryKey : countMap.entrySet()) {
            objects.add(new LsofLineGroupBy().setKey(entryKey.getKey()).setCount(entryKey.getValue()));
        }
        objects.sort((a, b) -> b.getCount() - a.getCount());
        return objects;
    }

    /**
     * groupByRemoteHost
     *
     * @return
     */
    private List<LsofLineGroupBy> groupByRemoteHost(List<LsofLine> lsofLineList) {
        List<LsofLineGroupBy> objects = Lists.newArrayListWithCapacity(lsofLineList.size());
        HashMap<String, Integer> tmpMap = Maps.newHashMap();
        for (LsofLine lsofLine : lsofLineList) {
            String removeHost = lsofLine.getRemoveHost();
            Integer integer = tmpMap.get(removeHost);
            if (integer == null) {
                tmpMap.put(removeHost, 1);
            } else {
                tmpMap.put(removeHost, integer + 1);
            }
        }

        for (Map.Entry<String, Integer> stringIntegerEntry : tmpMap.entrySet()) {
            objects.add(new LsofLineGroupBy().setKey(stringIntegerEntry.getKey()).setCount(stringIntegerEntry.getValue()));
        }
        objects.sort((a, b) -> b.getCount() - a.getCount());
        return objects;
    }

    @Data
    @Accessors(chain = true)
    public static class LsofLine {
        private long deviceId;
        private String name;

        private int localPort;

        private String removeHost;
        private String remoteAppkey;
    }

    @Data
    @Accessors(chain = true)
    public static class LsofLineGroupBy {
        private String key;
        private int count;
    }


}
