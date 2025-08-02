package com.mk.springlangchain4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mk.springlangchain4j.entity.ChatConversationEntity;

import java.util.List;

/**
 * 对话主表 服务类
 */
public interface ChatConversationService extends IService<ChatConversationEntity> {

    ///**
    // * 根据用户ID获取对话列表
    // * @param userId 用户ID
    // * @return 对话列表
    // */
    //List<ChatConversationEntity> getConversationsByUserId(String userId);
    //
    ///**
    // * 创建新对话
    // * @param userId 用户ID
    // * @param title 对话标题
    // * @return 创建的对话
    // */
    //ChatConversationEntity createConversation(String userId, String title);
    //
    ///**
    // * 结束对话
    // * @param conversationId 对话ID
    // * @return 是否成功
    // */
    //boolean endConversation(Long conversationId);
    //
    ///**
    // * 更新对话标题
    // * @param conversationId 对话ID
    // * @param title 新标题
    // * @return 是否成功
    // */
    //boolean updateTitle(Long conversationId, String title);
}