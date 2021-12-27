package com.github.everything.service;

import com.github.everything.FlowContent;
import com.github.everything.output.dao.DBParam;
import com.github.everything.output.dao.OneDao;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author wangquan07
 * 2021/12/16 23:04
 */
@Slf4j
@Component
public class DBOuter {
    @Autowired
    private OneDao oneDao;

    public int upsert(FlowContent flowContent) {
        // 根据源数据，得到结果数据
        DBParam dbParam = new DBParam();
        dbParam.setTableName(flowContent.getTableName());
        // 最终的datamap是aftMap与cols的交集
        HashMap<String, Object> dbMap = Maps.newHashMap();
        for (String col : flowContent.getAftMap().keySet()) {
            if (!flowContent.getConfigCols().contains(col)) {
                continue;
            }
            dbMap.put(col, flowContent.getAftMap().get(col));
        }
        dbParam.setDbMap(dbMap);
        return oneDao.upsert(dbParam);
    }

    public int delete(FlowContent flowContent) {
        DBParam dbParam = new DBParam();
        dbParam.setTableName(flowContent.getTableName());
        HashMap<String, Object> dbMap = Maps.newHashMap();
        for (String uniqueCol : flowContent.getConfigUniqueCol()) {
            Object o = flowContent.getPreMap().get(uniqueCol);
            if (o == null) {
                log.error("DBOuter.delete preMap中不包含uniqueCol全部字段");
                return -1;
            }
            dbMap.put(uniqueCol, o);
            dbParam.setDbMap(dbMap);
        }
        return oneDao.delete(dbParam);
    }
}
