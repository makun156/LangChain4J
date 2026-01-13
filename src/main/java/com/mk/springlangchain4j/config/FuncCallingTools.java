package com.mk.springlangchain4j.config;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class FuncCallingTools {

    @Tool("求两数之和")
    public int sum(int a, int b){
        log.info("求两数之和");
        return a + b;
    }
    @Tool("求两数之差")
    public int sub(int a, int b){
        log.info("求两数之差");
        return a - b;
    }
    @Tool("当前时间")
    public String now(){
        log.info("当前时间");
        return LocalDateTime.now().toString();
    }
}
