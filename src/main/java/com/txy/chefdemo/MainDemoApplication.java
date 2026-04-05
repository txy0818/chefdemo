package com.txy.chefdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */

@SpringBootApplication
@EnableScheduling
@EnableAsync
//@MapperScan("com.txy.plugindemo.mapper")
public class MainDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainDemoApplication.class, args);
    }
}
