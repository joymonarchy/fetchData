package com.hust.ewsystemdatafetcher.service;

import com.hust.ewsystemdatafetcher.entity.CommonData;
import com.hust.ewsystemdatafetcher.mapper.CommonDataMapper;
import com.yingfeng.api.YFNowval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DataService {

    private final CommonDataMapper dataRecordMapper;

    public DataService(CommonDataMapper dataRecordMapper) {
        this.dataRecordMapper = dataRecordMapper;
    }

    @Transactional
    public void saveBatchData(HashMap<String, List<CommonData>> dataMap) {
        for (Map.Entry<String, List<CommonData>> entry : dataMap.entrySet()) {
            String tableName = entry.getKey();
            List<CommonData> records = entry.getValue();
            if (!records.isEmpty()) {
                dataRecordMapper.batchInsertIntoTable(tableName, records);
            }
        }
    }

    public void processAndSaveData(List<YFNowval> values) {
        if (values != null && !values.isEmpty()) {
            // 使用 HashMap 按 tableName 分组
            HashMap<String, List<CommonData>> dataMap = new HashMap<>();
            for (YFNowval item : values) {
                if (item.value.Status == 1) {
                    String cpid = item.Cpid.toLowerCase();
                    CommonData record = new CommonData();
                    record.setDatetime(new Date()); // 设置为当前时间
                    record.setStatus(item.value.Status);
                    record.setValue(item.value.Value);
                    System.out.println(cpid+"\t"+record.getValue());

                    dataMap.computeIfAbsent(cpid, k -> new ArrayList<>()).add(record);
                }
            }
            // 批量插入
            saveBatchData(dataMap);
        } else {
            System.out.println("未获取到任何数据.");
        }
    }
}