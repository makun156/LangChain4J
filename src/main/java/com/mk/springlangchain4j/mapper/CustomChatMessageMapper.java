package com.mk.springlangchain4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.springlangchain4j.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 对话消息表 Mapper 接口
 */

public interface CustomChatMessageMapper extends BaseMapper<ChatMessageEntity> {

}