package com.mk.springlangchain4j.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mk.springlangchain4j.entity.ChatMessageEntity;
import com.mk.springlangchain4j.enums.MessageRole;
import com.mk.springlangchain4j.mapper.CustomChatConversationMapper;
import com.mk.springlangchain4j.mapper.CustomChatMessageMapper;
import com.mk.springlangchain4j.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话消息表 服务实现类
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<CustomChatMessageMapper, ChatMessageEntity> implements ChatMessageService {

    //@Override
    //public List<ChatMessageEntity> getMessagesByConversationId(Long conversationId) {
    //    QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
    //    queryWrapper.eq("conversation_id", conversationId)
    //            .orderByAsc("message_time");
    //    return list(queryWrapper);
    //}
    //
    //@Override
    //public ChatMessageEntity saveUserMessage(Long conversationId, String content) {
    //    ChatMessageEntity message = ChatMessageEntity.builder()
    //            .conversationId(conversationId)
    //            .role(MessageRole.USER)
    //            .content(content)
    //            .messageTime(LocalDateTime.now())
    //            .createdAt(LocalDateTime.now())
    //            .build();
    //
    //    save(message);
    //    return message;
    //}
    //
    //@Override
    //public ChatMessageEntity saveAiMessage(Long conversationId, String content) {
    //    ChatMessageEntity message = ChatMessageEntity.builder()
    //            .conversationId(conversationId)
    //            .role(MessageRole.AI)
    //            .content(content)
    //            .messageTime(LocalDateTime.now())
    //            .createdAt(LocalDateTime.now())
    //            .build();
    //    save(message);
    //    return message;
    //}
    //
    //@Override
    //public boolean saveMessages(List<ChatMessageEntity> messages) {
    //    return saveBatch(messages);
    //}
    //
    //@Override
    //public boolean deleteMessagesByConversationId(Long conversationId) {
    //    QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
    //    queryWrapper.eq("conversation_id", conversationId);
    //    return remove(queryWrapper);
    //}
}