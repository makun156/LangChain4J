package com.mk.springlangchain4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mk.springlangchain4j.entity.ChatConversationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

/**
 * 对话主表 Mapper 接口
 */

public interface CustomChatConversationMapper extends BaseMapper<ChatConversationEntity> {

}