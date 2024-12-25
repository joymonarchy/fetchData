package com.hust.ewsystemdatafetcher.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CommonData {

    // 不使用 @TableName，因为表名动态指定

    private Date datetime;
    private Integer status;
    private Double value;

    // 获取动态表名的方法（可选，根据实际需要实现）
}