package com.mk.springlangchain4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话主表实体类
 */
@Data
@Builder
@TableName("chat_conversation")
public class ChatConversationEntity implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    /**
     * 用户标识
     */
    @TableField("user_id")
    public String userId;

    /**
     * 会话id
     */
    @TableField("session_id")
    public String sessionId;

    /**
     * 对话标题（可由首条消息生成）
     */
    @TableField("title")
    public String title;

    /**
     * 会话开始时间
     */
    @TableField("start_time")
    public LocalDateTime startTime;

    /**
     * 会话结束时间
     */
    @TableField("end_time")
    public LocalDateTime endTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    public LocalDateTime createdAt;
}