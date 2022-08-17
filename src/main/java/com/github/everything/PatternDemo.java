package com.github.everything;

import org.apache.commons.lang3.StringUtils;

public class PatternDemo {
    public static void main(String[] args) {
        System.out.println(1);
        System.out.println(-1);
        int a  = -1;
        System.out.println(-a);
        String tableName = "L_1_3";

        String replaceTableName = StringUtils.replacePattern(tableName, "[0-9]+$", "x");
        System.out.println(replaceTableName);
    }
}
