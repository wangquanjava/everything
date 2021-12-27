package com.github.everything;

import com.github.everything.input.binlog.EventType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author wangquan07
 * 2021/12/16 23:14
 */
@Data
public class FlowContent {
    // ↓↓↓↓↓binlog类型源数据↓↓↓↓↓
    /**
     * 本次binlog类型
     */
    private String tableName;
    /**
     * 本次binlog类型
     */
    private EventType eventType;
    /**
     * 更改前数据，insert时为空
     */
    private Map<String, Object> preMap;
    /**
     * 更改后数据，delete时为空
     */
    private Map<String, Object> aftMap;
    /**
     * 更改字段，delete时为全部字段
     */
    private List<String> diffCols;





    // ↓↓↓↓↓DB类型输出配置↓↓↓↓↓
    /**
     * 输出的字段
     */
    private List<String> configCols;

    /**
     * 唯一键，用来确保insert on update可用，该字段必须要在上面的cols字段中
     */
    private List<String> configUniqueCol;

}
