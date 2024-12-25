package com.hust.ewsystemdatafetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EwsystemdataFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(EwsystemdataFetcherApplication.class, args);
    }
}
