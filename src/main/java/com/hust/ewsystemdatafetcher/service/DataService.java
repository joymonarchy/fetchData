package com.hust.ewsystemdatafetcher.service;

import com.hust.ewsystemdatafetcher.entity.CommonData;
import com.hust.ewsystemdatafetcher.mapper.CommonDataMapper;
import com.yingfeng.api.YFHisval;
import com.yingfeng.api.YFNowval;
import com.yingfeng.api.YFValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void processAndSaveNowData(List<YFNowval> values) {
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

    public void processAndSaveHisData(List<YFHisval> values) {
        if (values != null && !values.isEmpty()) {
            // 使用 Streams 进行数据转换和分组
            HashMap<String, List<CommonData>> dataMap = values.stream()
                    // 过滤掉 Cpid 为 null 的记录（可选）
                    .filter(hisval -> hisval.getCpid() != null)
                    // 进行扁平化处理，将每个 YFHisval 的 YFValue 转换为 CommonData，并关联表名
                    .flatMap(hisval -> {
                        String tableName = hisval.getCpid().toLowerCase();


                        List<YFValue> yfValues = hisval.getValues();
                        if (yfValues == null || yfValues.isEmpty()) {
                            return Stream.empty();
                        }

                        return yfValues.stream().map(yfValue -> {
                            CommonData record = new CommonData();
                            record.setDatetime(yfValue.Time); // 使用 YFValue 的时间
                            record.setStatus(yfValue.Status);
                            record.setValue(yfValue.Value);
                            return new AbstractMap.SimpleEntry<>(tableName, record);
                        });
                    })
                    // 按表名分组
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            HashMap::new, // 显式指定使用 HashMap
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                    ));

            // 批量插入
            saveBatchData(dataMap);

        } else {
            System.out.println("未获取到任何数据.");
        }
    }

    /**
     * 验证表名是否合法，防止 SQL 注入
     *
     * @param tableName 表名
     * @return 如果合法返回 true，否则返回 false
     */

}