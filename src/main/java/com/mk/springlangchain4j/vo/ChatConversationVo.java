package com.mk.springlangchain4j.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatConversationVo {
    /**
     * 主键ID
     */
    public Long id;

    /**
     * 用户标识
     */
    public String userId;

    /**
     * 会话id
     */
    public String sessionId;

    /**
     * 对话标题（可由首条消息生成）
     */
    public String title;

    /**
     * 当前会话历史条数
     */
    public int total;

    /**
     * 会话开始时间
     */
    public LocalDateTime startTime;

    /**
     * 会话结束时间
     */
    public LocalDateTime endTime;

    /**
     * 创建时间
     */
    public LocalDateTime createdAt;
}
