package com.mk.springlangchain4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mk.springlangchain4j.enums.MessageRole;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息表实体类
 */
@Data
@Builder
@TableName("chat_message")
public class ChatMessageEntity implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    /**
     * 关联对话ID
     */
    @TableField("conversation_id")
    public Long conversationId;

    /**
     * 角色：USER/AI
     */
    @TableField("role")
    public MessageRole role;

    /**
     * 消息内容
     */
    @TableField("content")
    public String content;

    /**
     * 消息时间
     */
    @TableField("message_time")
    public LocalDateTime messageTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    public LocalDateTime createdAt;
}