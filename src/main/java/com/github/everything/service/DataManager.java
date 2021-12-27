package com.github.everything.service;

import com.github.everything.DTO.Flow;
import com.github.everything.DTO.Input;
import com.github.everything.dao.DataManagerDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wangquan07
 * 2021/12/18 23:45
 */
@Service
public class DataManager {
    @Autowired
    private DataManagerDao dataManagerDao;
    // map<input, list<flow>>
    private Map<String, List<Flow>> flowByInputKey;

    @PostConstruct
    public void init() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void load() {
        List<Input> inputs = dataManagerDao.selectInputAll();
        Map<Integer, Input> inputById = inputs.stream().collect(Collectors.toMap(Input::getId, a -> a));

        List<Flow> flows = dataManagerDao.selectFlowAll();
        Map<String, List<Flow>> flowByInputKeyMap = Maps.newHashMap();

        for (Flow flow : flows) {
            String inputKey = getInputKey(inputById.get(flow.getInput_id()));
            List<Flow> tmp = flowByInputKeyMap.get(inputKey);
            if (tmp == null) {
                tmp = Lists.newArrayList();
                flowByInputKeyMap.put(inputKey, tmp);
            }
            tmp.add(flow);
        }
        this.flowByInputKey = flowByInputKeyMap;
    }

    private String getInputKey(Input input) {
        return input.getDb_name() + "_" + input.getTable_name();
    }

    public Map<String, List<Flow>> getMap() {
        return this.flowByInputKey;
    }

}
