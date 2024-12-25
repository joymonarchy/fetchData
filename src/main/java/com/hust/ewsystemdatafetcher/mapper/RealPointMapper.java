package com.hust.ewsystemdatafetcher.mapper;

/**
 * @Author piiaJet
 * @Create 2024/12/2417:40
 */


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hust.ewsystemdatafetcher.entity.RealPoint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RealPointMapper extends BaseMapper<RealPoint> {
    // 基本的 CRUD 操作由 BaseMapper 提供，无需额外定义
}
