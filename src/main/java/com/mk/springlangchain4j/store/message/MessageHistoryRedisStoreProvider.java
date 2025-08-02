package com.mk.springlangchain4j.store.message;

import com.mk.springlangchain4j.config.SessionContext;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MessageHistoryRedisStoreProvider implements ChatMemoryStore {
    public static final String MESSAGE_HISTORY_PREFIX = "message_record:";
    @Autowired
    StringRedisTemplate redis;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<String> messageHistoryList = redis.opsForList().range(MESSAGE_HISTORY_PREFIX + memoryId, 0, -1);
        List<ChatMessage> collect = messageHistoryList.stream().map(ChatMessageDeserializer::messageFromJson).toList();
        log.info("getMessages的messageHistory:{}",collect);
        return collect;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        log.info("updateMessages的messageHistory:{}",messages);
        redis.opsForList().rightPush(MESSAGE_HISTORY_PREFIX + memoryId,ChatMessageSerializer.messageToJson(messages.get(messages.size()-1)));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.delete(MESSAGE_HISTORY_PREFIX + memoryId);
    }
}
