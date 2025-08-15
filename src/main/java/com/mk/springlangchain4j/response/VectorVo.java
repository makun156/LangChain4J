package com.mk.springlangchain4j.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VectorVo {
    /**
     * 向量id
     */
    public String vectorId;

    /**
     * 相似度
     */
    public String vectorScore;

    /**
     * 文本内容
     */
    public String text;

    /**
     * 元数据
     */
    public String metadata;

}
