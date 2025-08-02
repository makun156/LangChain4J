package com.mk.springlangchain4j.config;

import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
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
    @Autowired
    EmbeddingModel embeddingModel;
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

    @Bean
    public EmbeddingStore<TextSegment> milvusEmbeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host("49.232.163.127")
                .port(19530)
                .databaseName("default")
                .collectionName("data")
                .dimension(1024)// 向量维度
                .indexType(IndexType.IVF_SQ8)// 索引类型
                .metricType(MetricType.COSINE)// 距离度量类型 使用余弦相似度
                .consistencyLevel(ConsistencyLevelEnum.BOUNDED) // 一致性级别
                .autoFlushOnInsert(true)// 插入后自动刷新
                .idFieldName("id")                   // ID字段名（INT64，手动指定）
                .vectorFieldName("vector")           // 向量字段名（Float Vector）
                .textFieldName("text")               // 文本字段名（VarChar）
                .metadataFieldName("metadata")
                .build();
    }
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> milvusEmbeddingStore){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(milvusEmbeddingStore)
                .embeddingModel(embeddingModel)
                .build();
    }
}
