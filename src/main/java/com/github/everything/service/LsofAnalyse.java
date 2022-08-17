package com.github.everything.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LsofAnalyse {
    private Map<String, String> appkeyByHostKey = Maps.newHashMap();

    public static void main(String[] args) {
        new LsofAnalyse().printSourceHostList("/Users/wangquan07/Downloads/lbs08082250.lsof.log",
                "30033",
                "s_u_745896=wPzGrzS+8NIHAQMhaT8k6w==; _lxsdk_cuid=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; _lxsdk=170be73212cc8-0731fedd72247-39687407-13c680-170be73212cc8; moa_deviceId=CC10A83ECE405AAB800281D5C443DAE5; u=1412433641; _lx_utm=utm_source%3Dxm; s_m_id_3299326472=AwMAAAA5AgAAAAIAAAE9AAAALB+BkVx3BsC6IiqdGxjomEgnDMuMIu0bpTDgI2WskTQbXNwpv9HLjVAUdZEOAAAAIxPYjajjcYPLgk8Qx8txZLizwUKQNcRfxakorATzHYZOZUyf; _lxsdk_s=1827dd026fe-0a1-c36-f3c%7C%7C72; csrftoken=OpcockFIAtgwQsJCujJFYfCmIRzkjGYT; sessionid=v8jsuqe0ew8qrjqrizuqxorkcggs9y9m; ssoid=eAGFjj1Lw0AYgDkRLZ0kk2PGNhB47_Le5c7JNA04-jEILnJfGfUPONhuBScndVIotqBY3II4CeKskzjZQc3kb1ARZ3nWh4enQRbupgMSvlTV0RhY0ykUEnRpYCmkqLnmUnKRCHQMNDUisuDLMjGKsrTzTIL2pjcb1u_4vTynkMmkyAsEnmUdCcAk7fIcMelmBQ8Pnu4nl9CaYf-G5c_S8vxK_2b6OYLVx4fX23Pok6g5t7ae7zofBG_DUX199j64-Djt1VfDetJbnA33q-N261c-JI2_sRPCJCudLdM0ZparGLVQsQRqYl9q4AK8EuC2qeDqGwSgyLdCaX2SCA6oI4EaU6NsylLrlEVpPHNfXZNmMw**eAEFwYEBACAEBMCV8KEfR8r-I3TXJBhuL1GjjI1qB20ibUnWokHa1Sb3uXka-ZqiRtwbMx8S5hDg**UtYT0zr-8MpJ5QRNbToWgBNv8VWaqMjKnTnzAmpWz8kZfpjlEb_72xlnb5p0NCsDYBVx3EzQLGJMKGNnItwHWg**MjI1NzA5MCx3YW5ncXVhbjA3LOeOi-adgyx3YW5ncXVhbjA3QG1laXR1YW4uY29tLDEsMDMwNjQ5MDgsMTY2MDA1NjExNjMxNA");
    }

    /**
     * 分析出调用端 -> 自身的appkey分布+port分布
     *
     * @param filePath   eg:机器上lsof -p [pid]的结果
     * @param portString eg:8001,8002
     * @param opsCookie  eg:cookie获取：https://docs.sankuai.com/ops/auth/
     */
    public void printSourceAppkeyAndPortList(String filePath, String portString, String opsCookie) {
        String[] split = StringUtils.split(filePath, ",");
        ArrayList<LsofLine> data = Lists.newArrayList();
        for (String s : split) {
            data.addAll(file2List(s, opsCookie));
        }
        List<Integer> serverPortList = Lists.newArrayList(StringUtils.split(portString, ",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        List<LsofLine> serverList = filterServerList(data, serverPortList);
        List<LsofLineGroupBy> lsofLineGroupBIES = groupPortByAppKey(serverList);
        String format = "%60s : %60s : %11s";
        log.info(String.format(format, "appkey", "portList", "tcpCount"));
        for (LsofLineGroupBy lsofLineGroupBy : lsofLineGroupBIES) {
            log.info(String.format(format, lsofLineGroupBy.getKey(), lsofLineGroupBy.getValue(), lsofLineGroupBy.getCount()));
        }

    }

    /**
     * 分析出调用端 -> 自身的appkey分布
     *
     * @param filePath   eg:机器上lsof -p [pid]的结果
     * @param portString eg:8001,8002
     * @param opsCookie  eg:cookie获取：https://docs.sankuai.com/ops/auth/
     */
    public void printSourceAppkeyList(String filePath, String portString, String opsCookie) {
        String[] split = StringUtils.split(filePath, ",");
        ArrayList<LsofLine> data = Lists.newArrayList();
        for (String s : split) {
            data.addAll(file2List(s, opsCookie));
        }
        List<Integer> serverPortList = Lists.newArrayList(StringUtils.split(portString, ",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        List<LsofLine> serverList = filterServerList(data, serverPortList);
        List<LsofLineGroupBy> lsofLineGroupBIES = groupByAppKey(serverList);
        String format = "%60s : %11s";
        log.info(String.format(format, "appkey", "createCount"));
        for (LsofLineGroupBy lsofLineGroupBy : lsofLineGroupBIES) {
            log.info(String.format(format, lsofLineGroupBy.getKey(), lsofLineGroupBy.getCount()));
        }
    }

    /**
     * 分析出调用端host分布
     *
     * @param filePath   eg:机器上lsof -p [pid]的结果
     * @param portString eg:8001,8002
     * @param opsCookie  eg:cookie获取：https://docs.sankuai.com/ops/auth/
     */
    public void printSourceHostList(String filePath, String portString, String opsCookie) {
        String[] split = StringUtils.split(filePath, ",");
        ArrayList<LsofLine> data = Lists.newArrayList();
        for (String s : split) {
            data.addAll(file2List(s, opsCookie));
        }
        List<Integer> serverPortList = Lists.newArrayList(StringUtils.split(portString, ",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        List<LsofLine> serverList = filterServerList(data, serverPortList);
        List<LsofLineGroupBy> lsofLineGroupBIES = groupByRemoteHost(serverList);
        String format = "%60s : %11s";
        log.info(String.format(format, "host", "count"));
        for (LsofLineGroupBy lsofLineGroupBy : lsofLineGroupBIES) {
            log.info(String.format(format, lsofLineGroupBy.getKey(), lsofLineGroupBy.getCount()));
        }
    }

    /**
     * 指定文件位置，进行解析
     */
    private List<LsofLine> file2List(String path, String opsCookie) {
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
                        .setRemoteAppkey(getRemoteAppkey(name, opsCookie));
                result.add(lsofLine);
            } catch (Exception e) {
                errorCount++;
                continue;
            }
        }
        log.info("转换完成，lsof格式不匹配次数:{}", errorCount);
        return result;
    }

    /**
     * 解析出本地端口
     *
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     * @return 8011
     */
    private int getLocalPort(String name) {
        List<String> split = Splitter.on("->").splitToList(name);
        name = split.get(0);
        List<String> split1 = Splitter.on(":").splitToList(name);
        return Integer.parseInt(split1.get(1));
    }

    /**
     * 解析出远程端口
     *
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     * @return set-zf-order-api-managerserver53.mt
     */
    private String getRemoteHost(String name) {
        List<String> split = Splitter.on("->").splitToList(name);
        name = split.get(1);
        List<String> split1 = Splitter.on(":").splitToList(name);
        return split1.get(0);
    }

    /**
     * 解析出远程appkey
     *
     * @param name 示例set-zf-tsp-uds-cbase01.mt:8011->set-zf-order-api-managerserver53.mt:14922
     * @return com.sankuai.waimai.cbase
     */
    private String getRemoteAppkey(String name, String opsCookie) {
        String host = getRemoteHost(name);
        String hostKey = getHostKey(name);
        String appkey = appkeyByHostKey.get(hostKey);
        if (appkey != null) {
            return appkey;
        }
        appkey = getAppekeyFromOps(host, opsCookie);
        if (StringUtils.isNotBlank(appkey)) {
            appkeyByHostKey.put(hostKey, appkey);
            return appkey;
        }
        return hostKey;
    }

    /**
     * 从name中获取host key，方便本地缓存
     */
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
     * 根据主机名从ops获取appkey
     */
    private String getAppekeyFromOps(String host, String opsCookie) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URIBuilder(String.format("http://ops.vip.sankuai.com/api/v0.2/hosts/%s/appkeys", host)).build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Cookie", opsCookie);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                // 如果http状态码是200, 则取出响应体, 序列化为string
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity, "utf-8");
                JSONObject jsonObject = JSON.parseObject(content);
                JSONArray appkeys = jsonObject.getJSONArray("appkeys");
                if (appkeys == null) {
                    log.error("host解析异常,host:{}", host);
                    return "";
                }
                return appkeys.get(0).toString();
            }
        } catch (Exception e) {
            log.error("请求ops获取appkeys异常", e);
        }
        return "";
    }

    private List<LsofLine> filterServerList(List<LsofLine> data, List<Integer> serverPortList) {
        List<LsofLine> result = Lists.newArrayListWithCapacity(data.size());
        for (LsofLine datum : data) {
            if (serverPortList.contains(datum.getLocalPort())) {
                result.add(datum);
            }
        }
        return result;
    }
    private List<LsofLineGroupBy> groupPortByAppKey(List<LsofLine> lsofLinesPre) {
        List<LsofLineGroupBy> objects = Lists.newArrayList();
        ImmutableListMultimap<String, LsofLine> index = Multimaps.index(lsofLinesPre, new Function<LsofLine, String>() {
            @Override
            public String apply(LsofLine input) {
                return input.getRemoteAppkey();
            }
        });
        for (String remoteAppkey : index.keySet()) {
            ImmutableList<LsofLine> lsofLines = index.get(remoteAppkey);
            Set<Integer> portSet = Sets.newHashSet();
            int count = 0;
            for (LsofLine lsofLine : lsofLines) {
                count++;
                portSet.add(lsofLine.getLocalPort());
            }
            objects.sort((a, b) -> b.getCount() - a.getCount());
            objects.add(new LsofLineGroupBy().setKey(remoteAppkey).setCount(count).setValue(Joiner.on(",").join(portSet)));
        }
        return objects;
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

    private List<LsofLine> getDiff(List<LsofLine> data1, List<LsofLine> data2) {
        List<Long> data1DeviceId = data1.stream().map(LsofLine::getDeviceId).collect(Collectors.toList());
        List<Long> data2DeviceId = data2.stream().map(LsofLine::getDeviceId).collect(Collectors.toList());
        List<Long> subtract = ListUtils.subtract(data1DeviceId, data2DeviceId);
        Map<Long, LsofLine> map = data1.stream().collect(Collectors.toMap(LsofLine::getDeviceId, a -> a));
        return subtract.stream().map(a -> map.get(a)).collect(Collectors.toList());
    }

    private String printDiffSummaryByRemoteHost(List<LsofLineGroupBy> data, List<LsofLine> now) {
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


    private List<LsofLine> filterClientList(List<LsofLine> data, List<Integer> serverPortList) {
        List<LsofLine> result = Lists.newArrayListWithCapacity(data.size());
        for (LsofLine datum : data) {
            if (!serverPortList.contains(datum.getLocalPort())) {
                result.add(datum);
            }
        }
        return result;
    }

    @Data
    @Accessors(chain = true)
    private static class LsofLine {
        private long deviceId;
        private String name;

        private int localPort;

        private String removeHost;
        private String remoteAppkey;
    }

    @Data
    @Accessors(chain = true)
    private static class LsofLineGroupBy {
        private String key;
        private int count;
        private String value;
    }
}
