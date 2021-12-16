package com.github.everything;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author wangquan07
 * 2021/12/14 23:38
 */
@Mapper
public interface OneDao {

    @Select("#{sql}")
    public List<Map<String, Object>> select(String sql);
}
