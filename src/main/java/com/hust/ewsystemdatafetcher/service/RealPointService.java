package com.hust.ewsystemdatafetcher.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hust.ewsystemdatafetcher.entity.RealPoint;
import com.hust.ewsystemdatafetcher.mapper.RealPointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealPointService {

    @Autowired
    private RealPointMapper realPointMapper;

    /**
     * 获取所有的 vcpids
     *
     * @return vcpid 列表
     */
    public List<String> getAllVcpids() {
        QueryWrapper<RealPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("point_label");
        List<RealPoint> realPoints = realPointMapper.selectList(queryWrapper);
        return realPoints.stream()
                .map(RealPoint::getPointLabel)
                .filter(label -> label != null && !label.trim().isEmpty())
                .collect(Collectors.toList());
    }
}
