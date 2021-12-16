package com.github.everything;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;

import java.io.File;
import java.util.List;

/**
 * @author wangquan07
 * 2021/12/15 23:40
 */
public class MafkaExcel {
    public static void main(String[] args) {
        File[] files1 = new File("/Users/wangquan07/Downloads/message 6").listFiles();

        int count = 0;
        for (File file : files1) {
            System.out.println("开始加载" + file.getName());
            ExcelReader reader = ExcelUtil.getReader(file);
            List<List<Object>> read = reader.read(1);
            for (List<Object> objects : read) {
                String msg = (String) objects.get(6);
                if (msg.contains("130887722")) {
                    System.out.println(file.getName());
                    System.out.println(objects);
                }
            }
            System.out.println("加载完成" + file.getName());
            reader.close();
            count++;
        }
        System.out.println(count);

    }
}
