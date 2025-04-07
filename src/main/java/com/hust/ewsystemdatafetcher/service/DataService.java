package com.hust.ewsystemdatafetcher.service;

import com.hust.ewsystemdatafetcher.entity.CommonData;
import com.hust.ewsystemdatafetcher.mapper.CommonDataMapper;
import com.yingfeng.api.YFHisval;
import com.yingfeng.api.YFNowval;
import com.yingfeng.api.YFValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @Async
    public void processAndSaveNowData(List<YFNowval> values) {
        if (values != null && !values.isEmpty()) {
            HashMap<String, List<CommonData>> dataMap = new HashMap<>();

            // 预计算当前时间戳
            LocalDateTime currentTime = LocalDateTime.now()
                    .withSecond((LocalDateTime.now().getSecond() / 10) * 10) // 取整到10秒倍数
                    .withNano(0);
            Date currentDate = Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant());
            final long currentTimestamp = currentDate.getTime(); // 一次性计算时间戳

            values.parallelStream()
                    .filter(item -> item.value.Status == 1) // 初始过滤状态为1的项
                    .forEach(item -> {
                        String cpid = item.Cpid.toLowerCase();
                        CommonData record = new CommonData();
                        record.setDatetime(currentDate);
                        record.setValue(item.value.Value);

                        // 时间差判断（毫秒计算优化性能）
                        long itemTimestamp = item.value.Time.getTime();
                        long timeDiff = currentTimestamp - itemTimestamp;
                        int calculatedStatus = (timeDiff >= 1800000L) ? 0 : 1; // 超30min则Status=0

                        record.setStatus(calculatedStatus);
                        System.out.println(cpid+"\t"+record.getDatetime()+"\t"+record.getValue()+'\t'+record.getStatus() );

                        synchronized (dataMap) { // 线程安全操作Map
                            dataMap.computeIfAbsent(cpid, k -> new ArrayList<>())
                                    .add(record);
                        }
                    });

            saveBatchData(dataMap);
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