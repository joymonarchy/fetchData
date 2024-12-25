package com.hust.ewsystemdatafetcher.entity;

/**
 * @Author piiaJet
 * @Create 2024/12/2417:39
 */


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("real_point")
public class RealPoint {

    @TableId
    private Integer pointId;

    private String pointLabel;
    private String pointDescription;
    private Integer moduleId;
    private String pointUnit;
    private Integer pointType;
    private Integer turbineId;

    // 其他字段如果有的话
}
