package com.github.everything.output.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

/**
 * @author wangquan07
 * 2021/12/17 23:55
 */
@Slf4j
public class SQLBuilder {
    Joiner DOT = Joiner.on(",");

    /**
     * INSERT INTO `wm_c_poi` (`affect`, `m_id`)
     * VALUES (#{dbParam.affect}, #{dbParam.m_id})
     * ON DUPLICATE KEY UPDATE `affect` = #{dbParam.affect}, `m_id` = #{dbParam.m_id}
     *
     * @return
     */
    public String upsert(@Param("dbParam") DBParam dbParam) {
        ArrayList<String> cols = Lists.newArrayList();
        ArrayList<String> values = Lists.newArrayList();
        ArrayList<String> kvs = Lists.newArrayList();
        for (String col : dbParam.getDbMap().keySet()) {
            cols.add(col);
            String value = String.format("#{dbMap.%s}", col);
            values.add(value);
            kvs.add(String.format("%s = %s", col, value));
        }
        String result = String.format("INSERT INTO %s(%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s", dbParam.getTableName(), DOT.join(cols), DOT.join(values), DOT.join(kvs));
        log.info(result);
        return result;
    }

    /**
     * DELETE FROM `wm_c_poi` WHERE id = #{dbParam.id}, xx = #{dbParam.xx}
     *
     * @return
     */
    public String delete(@Param("dbParam") DBParam dbParam) {
        ArrayList<String> kvs = Lists.newArrayList();
        for (String col : dbParam.getDbMap().keySet()) {
            String value = String.format("#{dbMap.%s}", col);
            kvs.add(String.format("%s = %s", col, value));
        }
        String result = String.format("DELETE FROM %s WHERE %s", dbParam.getTableName(), DOT.join(kvs));
        log.info(result);
        return result;
    }
}
