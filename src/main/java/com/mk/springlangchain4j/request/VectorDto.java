package com.mk.springlangchain4j.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VectorDto {


    /**
     * 用户id
     */
    public String memoryId;

    /**
     * 相似度最小阈值
     */
    public String threshold;

    /**
     *输入内容
     */
    public String queryContent;

    public String ragContent;

    /**
     * 查询数量
     */
    public Integer queryTotal;
}
