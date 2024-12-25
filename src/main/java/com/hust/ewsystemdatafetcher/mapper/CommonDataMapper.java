package com.hust.ewsystemdatafetcher.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hust.ewsystemdatafetcher.entity.CommonData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommonDataMapper extends BaseMapper<CommonData> {

    @Insert("INSERT INTO ${tableName} (datetime, status, value) VALUES (#{record.datetime}, #{record.status}, #{record.value})")
    int insertIntoTable(@Param("tableName") String tableName, @Param("record") CommonData record);

    // 新增的批量插入方法
    @Insert({
            "<script>",
            "INSERT INTO ${tableName} (datetime, status, value) VALUES ",
            "<foreach collection='records' item='record' separator=','>",
            "(#{record.datetime}, #{record.status}, #{record.value})",
            "</foreach>",
            "</script>"
    })
    int batchInsertIntoTable(@Param("tableName") String tableName, @Param("records") List<CommonData> records);
}