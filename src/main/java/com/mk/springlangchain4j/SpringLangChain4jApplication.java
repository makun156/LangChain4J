package com.mk.springlangchain4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mk.springlangchain4j.mapper")
public class SpringLangChain4jApplication {

    public static void main(String[] args) {
        // 启动 Spring 应用
        SpringApplication.run(SpringLangChain4jApplication.class, args);
    }

}
