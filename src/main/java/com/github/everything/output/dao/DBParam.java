package com.github.everything.output.dao;

import lombok.Data;

import java.util.Map;

/**
 * @author wangquan07
 * 2021/12/17 23:57
 */
@Data
public class DBParam {
    private String tableName;
    /**
     * upsert: 需要存入DB的字段以及值
     * delete: delete删除条件，可以多条件
     */
    private Map<String, Object> dbMap;

}
