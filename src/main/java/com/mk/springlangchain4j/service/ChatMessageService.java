package com.mk.springlangchain4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mk.springlangchain4j.entity.ChatMessageEntity;

import java.util.List;

/**
 * 对话消息表 服务类
 */
public interface ChatMessageService extends IService<ChatMessageEntity> {

    ///**
    // * 根据对话ID获取消息列表
    // * @param conversationId 对话ID
    // * @return 消息列表
    // */
    //List<ChatMessageEntity> getMessagesByConversationId(Long conversationId);
    //
    ///**
    // * 保存用户消息
    // * @param conversationId 对话ID
    // * @param content 消息内容
    // * @return 保存的消息
    // */
    //ChatMessageEntity saveUserMessage(Long conversationId, String content);
    //
    ///**
    // * 保存AI消息
    // * @param conversationId 对话ID
    // * @param content 消息内容
    // * @return 保存的消息
    // */
    //ChatMessageEntity saveAiMessage(Long conversationId, String content);
    //
    ///**
    // * 批量保存消息
    // * @param messages 消息列表
    // * @return 是否成功
    // */
    //boolean saveMessages(List<ChatMessageEntity> messages);
    //
    ///**
    // * 删除对话的所有消息
    // * @param conversationId 对话ID
    // * @return 是否成功
    // */
    //boolean deleteMessagesByConversationId(Long conversationId);
}