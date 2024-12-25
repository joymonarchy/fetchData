package com.hust.ewsystemdatafetcher.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.hust.ewsystemdatafetcher.mapper")
public class MyBatisPlusConfig {
    // 可添加更多MyBatis-Plus的配置
}