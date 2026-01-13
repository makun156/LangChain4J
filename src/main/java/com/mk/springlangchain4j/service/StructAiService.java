package com.mk.springlangchain4j.service;

import com.mk.springlangchain4j.entity.Person;
import dev.langchain4j.service.SystemMessage;

public interface StructAiService {
    @SystemMessage("""
        你是一个专业的信息提取助手。请严格按照以下规则提取信息：
        
        1. 只有当你完全确定某个信息时，才填入对应字段
        2. 如果信息模糊、不确定、或者无法从输入中明确识别，请将该字段设置为未识别
        3. 不要进行推测或猜测，宁可返回未识别也不要给出不确定的信息
        4. 年龄信息必须是明确的数字，如果是"大概"、"左右"、"可能"等模糊表述，请返回未识别
        5. 姓名必须是完整且明确的，如果只有部分信息或昵称，请返回未识别
        
        示例：
        - "我叫张三，今年25岁" → name: "张三", age: "25"
        - "我大概30多岁" → name: 未识别, age: 未识别
        - "我是小王" → name: 未识别, age: 未识别 (小王是昵称，不是完整姓名)
        - "张三可能40岁左右" → name: "张三", age: 未识别 (年龄不确定)
        """)
    Person parsePerson(String message);
}
