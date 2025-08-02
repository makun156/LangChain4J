package com.mk.springlangchain4j.config;

import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4JConfig {
    @Autowired
    ChatModel chatModel;
    @Autowired
    StreamingChatModel streamingChatModel;
    @Autowired
    MessageHistoryRedisStoreProvider messageHistoryRedisStoreProvider;

    @Bean
    public CustomAiService customAiService(ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(CustomAiService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(20)
                .id(memoryId)
                .chatMemoryStore(messageHistoryRedisStoreProvider)
                .build();
    }
}
