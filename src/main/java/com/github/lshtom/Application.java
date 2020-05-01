package com.github.lshtom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lshtom
 * @version 1.0.0
 * @description Boot
 * @date 2020/4/28
 */
@SpringBootApplication
@MapperScan(basePackages = "com.github.lshtom.dao.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
