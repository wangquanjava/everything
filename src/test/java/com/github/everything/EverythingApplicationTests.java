package com.github.everything;

import com.alibaba.fastjson.JSON;
import com.github.everything.DTO.Flow;
import com.github.everything.dao.DataManagerDao;
import com.github.everything.output.dao.OneDao;
import com.github.everything.service.DBOuter;
import com.github.everything.service.DataManager;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class EverythingApplicationTests {
    @Autowired
    DataSource dataSource;
    @Autowired
    OneDao oneDao;
    @Autowired
    DBOuter dbOuter;
    @Autowired
    DataManagerDao dataManagerDao;
    @Autowired
    DataManager dataManager;

    @Test
    void contextLoads() {
        System.out.println(dataSource);

    }

    @Test
    void contextLoads_Upsert() {
        FlowContent flowContent = new FlowContent();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("id", 1111);
        map.put("name", "efsffsf");
        flowContent.setAftMap(map);
        flowContent.setTableName("product");

        flowContent.setConfigCols(Lists.newArrayList("id", "name"));
        int upsert = dbOuter.upsert(flowContent);
    }


    @Test
    void contextLoads_Delete() {
        FlowContent flowContent = new FlowContent();
        HashMap<String, Object> dbMap = Maps.newHashMap();
        dbMap.put("id", 2222);
        flowContent.setPreMap(dbMap);
        flowContent.setTableName("product");
        flowContent.setConfigUniqueCol(Lists.newArrayList("id"));
        int delete = dbOuter.delete(flowContent);
        System.out.println(delete);

    }

    @Test
    void contextLoads_load() throws InterruptedException {
        while (true) {
            Map<String, List<Flow>> map = dataManager.getMap();
            System.out.println(JSON.toJSONString(map, true));
            Thread.sleep(1000);
        }


    }


}
