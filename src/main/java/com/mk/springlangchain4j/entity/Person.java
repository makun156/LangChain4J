package com.mk.springlangchain4j.entity;

import lombok.Data;

@Data
public class Person {
    private String name;
    private String age;
    //成绩
    private String score;
    //排名
    private String ranking;
    //专业
    private String major;
    //地区
    private String location;
}
