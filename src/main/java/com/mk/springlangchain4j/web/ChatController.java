package com.mk.springlangchain4j.web;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mk.springlangchain4j.entity.ChatConversationEntity;
import com.mk.springlangchain4j.entity.ChatMessageEntity;
import com.mk.springlangchain4j.enums.MessageRole;
import com.mk.springlangchain4j.response.ResponseResult;
import com.mk.springlangchain4j.service.ChatConversationService;
import com.mk.springlangchain4j.service.ChatMessageService;
import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import com.mk.springlangchain4j.vo.ChatConversationVo;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    ChatModel chatModel;
    @Autowired
    StreamingChatModel streamingChatModel;
    @Autowired
    CustomAiService aiService;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ChatConversationService chatConversationService;
    @Autowired
    ChatMessageService chatMessageService;

    @GetMapping
    public ResponseResult chat(String message) {
        return ResponseResult.builder()
                .data(aiService.chat(message))
                .message("success")
                .status(200)
                .build();
    }

    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingChat(String message) {
        return aiService.streamingChat(message);
    }

    @PostMapping(value = "streamHis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingChatWithHistory(
            @RequestParam String memoryId,
            @RequestParam String sessionId,
            @RequestParam String userMessage) {
        return aiService.streamingChat(memoryId + ":" + sessionId, userMessage);
    }

    @Transactional
    @PostMapping("save")
    public ResponseResult save(@RequestParam String memoryId, @RequestParam String sessionId) {
        List<String> messageRecord = redis.opsForList().range("message_record:" + memoryId + ":" + sessionId, 0, -1);
        List<ChatMessage> chatMessages = messageRecord.stream().map(ChatMessageDeserializer::messageFromJson).toList();
        List<ChatMessageEntity> chatMessageRecords = new ArrayList<>();
        QueryWrapper<ChatConversationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", memoryId);
        queryWrapper.eq("session_id", sessionId);
        ChatConversationEntity exist = chatConversationService.getOne(queryWrapper);
        ChatConversationEntity chatConversation = null;
        if (exist == null) {
            chatConversation = ChatConversationEntity.builder()
                    .userId(memoryId)
                    .sessionId(sessionId)
                    .title(((TextContent) ((UserMessage) chatMessages.get(0)).contents().get(0)).text())
                    .startTime(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatConversationService.save(chatConversation);
        } else {
            chatConversation = exist;
        }
        for (ChatMessage itemRecord : chatMessages) {
            if (itemRecord.type() == ChatMessageType.AI) {
                // 处理AI消息
                ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                        .content(((AiMessage) itemRecord).text())
                        .conversationId(chatConversation.getId())
                        .role(MessageRole.AI)
                        .messageTime(LocalDateTime.now())
                        .build();
                chatMessageRecords.add(chatMessage);
            } else if (itemRecord.type() == ChatMessageType.USER) {
                // 处理用户消息
                ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                        .conversationId(chatConversation.getId())
                        .content(((TextContent) ((UserMessage) itemRecord).contents().get(0)).text())
                        .role(MessageRole.USER)
                        .messageTime(LocalDateTime.now())
                        .build();
                chatMessageRecords.add(chatMessage);
            }
        }
        QueryWrapper<ChatMessageEntity> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("conversation_id", chatConversation.getId());
        chatMessageService.getBaseMapper().delete(deleteWrapper);
        chatMessageService.saveBatch(chatMessageRecords);
        return ResponseResult.builder()
                .data("success")
                .message("success")
                .status(200)
                .build();
    }

    @GetMapping("getHistoryRecords")
    public ResponseResult getHistoryRecords(@RequestParam String memoryId) {
        QueryWrapper<ChatConversationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", memoryId);
        wrapper.orderByDesc("session_id");
        List<ChatConversationEntity> conversationEntityList = chatConversationService.getBaseMapper().selectList(wrapper);
        List<ChatConversationVo> list = new ArrayList<>();
        conversationEntityList.forEach(itemHistoryConversation -> {
            QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("conversation_id", itemHistoryConversation.getId());
            List<ChatMessageEntity> chatMessageRecords = chatMessageService.getBaseMapper().selectList(queryWrapper);
            ChatConversationVo chatConversationVo = BeanUtil.toBean(itemHistoryConversation, ChatConversationVo.class);
            chatConversationVo.setTotal(chatMessageRecords.size());
            list.add(chatConversationVo);
        });
        return ResponseResult.builder()
                .data(list)
                .message("success")
                .status(200)
                .build();
    }

    @Transactional
    @GetMapping("/conversation/detail")
    public ResponseResult getConversationDetail(@RequestParam("conversationId") Long conversationId) {
        QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        List<ChatMessageEntity> chatMessageEntities = chatMessageService.getBaseMapper().selectList(queryWrapper);

        QueryWrapper<ChatConversationEntity> queryConversationWrapper = new QueryWrapper<>();
        queryConversationWrapper.eq("id", conversationId);
        ChatConversationEntity conversation = chatConversationService.getOne(queryConversationWrapper);
        redis.delete(
                MessageHistoryRedisStoreProvider.MESSAGE_HISTORY_PREFIX +
                        conversation.getUserId() +
                        ":" +
                        conversation.getSessionId()
        );
        chatMessageEntities.forEach(itemChatMessageRecord->{
            ChatMessage chatMessage=null;
            if ("USER".equalsIgnoreCase(itemChatMessageRecord.getRole().getCode())) {
                chatMessage = UserMessage.builder()
                        .contents(List.of(new TextContent(itemChatMessageRecord.getContent())))
                        .build();
            }else if ("AI".equalsIgnoreCase(itemChatMessageRecord.getRole().getCode())){
                chatMessage=AiMessage.builder()
                        .text(itemChatMessageRecord.getContent())
                        .build();
            }
            redis.opsForList().rightPush(
                    MessageHistoryRedisStoreProvider.MESSAGE_HISTORY_PREFIX +
                            conversation.getUserId() +
                            ":" +
                            conversation.getSessionId(),
                    ChatMessageSerializer.messageToJson(chatMessage)
            );
        });
        return ResponseResult.builder()
                .message("success")
                .status(200)
                .data(chatMessageEntities)
                .build();
    }


}
