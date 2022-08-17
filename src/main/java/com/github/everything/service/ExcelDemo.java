package com.github.everything.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ExcelDemo {

    public static final BigDecimal MULTIPLICAND = new BigDecimal(100_0000);

    public static void main(String[] args) throws IOException {
        List<String> cityStrList = FileUtils.readLines(new File("/Users/wangquan07/Downloads/城市.txt"), Charset.defaultCharset());
        HashMap<String, List<HashMap<String, Integer>>> objectObjectHashMap = Maps.newHashMap();
        List<Region> result = Lists.newArrayList();
        for (int i = 1; i < cityStrList.size(); i++) {

            String cityStr = cityStrList.get(i);
            String[] split = StringUtils.split(cityStr, "\t");

            String cityName = split[1];
            String cityArea = split[12];
            // 87.735966,49.175215,87.723164,49.176187;!
            List<String> areaList = Lists.newArrayList();
            if (StringUtils.contains(cityArea, ";")) {
                String[] split1 = StringUtils.split(cityArea, ";");
                for (int j = 0; j < split1.length; j++) {
                    String s1 = split1[j];
                    if (s1.startsWith("!")) {
                        log.warn("遇到一块排除:name:{}, area:{}", cityName, JSON.toJSON(area2List(s1.substring(1))));
                        continue;
                    }
                    List<HashMap<String, Integer>> objects = area2List(s1);
                    areaList.add(JSON.toJSONString(objects));
                }
            } else {
                List<HashMap<String, Integer>> objects = area2List(cityArea);
                areaList.add(JSON.toJSONString(objects));
            }

            result.add(new Region(cityName, areaList));
        }

        FileUtils.writeStringToFile(new File("/Users/wangquan07/Downloads/cityRegion.log"), JSON.toJSONString(result, true), Charset.defaultCharset());
//        System.out.println();
    }
    @Data
    @AllArgsConstructor
    private static class Region {
        private String cityName;
        private List<String> areaList;
    }

    private static List<HashMap<String, Integer>> area2List(String area) {
        String[] xAndY = StringUtils.split(area, ",");
        List<HashMap<String, Integer>> objects = Lists.newArrayList();
        for (int j = 0; j < xAndY.length; j = j + 2) {
            try {
                HashMap<String, Integer> hashMap = new HashMap();
                String y = xAndY[j];
                String x = xAndY[j + 1];
                hashMap.put("x", new BigDecimal(x).multiply(MULTIPLICAND).intValue());
                hashMap.put("y", new BigDecimal(y).multiply(MULTIPLICAND).intValue());
                objects.add(hashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

}
