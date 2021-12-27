package com.github.everything.dao;

import com.github.everything.DTO.Flow;
import com.github.everything.DTO.Input;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wangquan07
 * 2021/12/18 23:52
 */
@Mapper
public interface DataManagerDao {
    @Select("select * from input")
    List<Input> selectInputAll();

    @Select("select * from flow")
    List<Flow> selectFlowAll();
}
