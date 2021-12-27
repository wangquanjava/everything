package com.github.everything.output.dao;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangquan07
 * 2021/12/14 23:38
 */
@Mapper
public interface OneDao {

    @InsertProvider(type = SQLBuilder.class, method = "upsert")
    int upsert(DBParam dbParam);

    @DeleteProvider(type = SQLBuilder.class, method = "delete")
    int delete(DBParam dbParam);

}
