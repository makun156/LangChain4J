package com.mk.springlangchain4j.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mk.springlangchain4j.entity.ChatConversationEntity;
import com.mk.springlangchain4j.mapper.CustomChatConversationMapper;
import com.mk.springlangchain4j.service.ChatConversationService;
import org.springframework.stereotype.Service;

/**
 * 对话主表 服务实现类
 */
@Service
public class ChatConversationServiceImpl extends ServiceImpl<CustomChatConversationMapper, ChatConversationEntity> implements ChatConversationService {

    //@Override
    //public List<ChatConversationEntity> getConversationsByUserId(String userId) {
    //    QueryWrapper<ChatConversationEntity> queryWrapper = new QueryWrapper<>();
    //    queryWrapper.eq("user_id", userId)
    //            .orderByDesc("created_at");
    //    return list(queryWrapper);
    //}
    //
    //@Override
    //public ChatConversationEntity createConversation(String userId, String title) {
    //    ChatConversationEntity conversation = ChatConversationEntity.builder()
    //            .userId(userId)
    //            .title(title)
    //            .startTime(LocalDateTime.now())
    //            .createdAt(LocalDateTime.now())
    //            .build();
    //
    //    save(conversation);
    //    return conversation;
    //}
    //
    //@Override
    //public boolean endConversation(Long conversationId) {
    //    UpdateWrapper<ChatConversationEntity> updateWrapper = new UpdateWrapper<>();
    //    updateWrapper.eq("id", conversationId)
    //            .set("end_time", LocalDateTime.now());
    //    return update(updateWrapper);
    //}
    //
    //@Override
    //public boolean updateTitle(Long conversationId, String title) {
    //    UpdateWrapper<ChatConversationEntity> updateWrapper = new UpdateWrapper<>();
    //    updateWrapper.eq("id", conversationId)
    //            .set("title", title);
    //    return update(updateWrapper);
    //}
}