package com.mk.springlangchain4j.config;

import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.service.VectorAiService;
import com.mk.springlangchain4j.service.WebSearchAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
    public CustomAiService customAiService(ChatMemoryProvider chatMemoryProvider, ContentRetriever contentRetriever) {
        return AiServices.builder(CustomAiService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    @Bean
    public VectorAiService vectorAiService(ChatMemoryProvider chatMemoryProvider, ContentRetriever contentRetriever) {
        return AiServices.builder(VectorAiService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(contentRetriever)
                .build();
    }

    @Value("${search.api.key:}")
    private String searchApiKey;

    @Value("${search.api.engine:google}")
    private String searchEngine;

    @Bean
    public WebSearchAiService webSearchAiService() {
        if (searchApiKey == null || searchApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Search API key is not configured. Please set 'search.api.key' in application.yml");
        }
        Map<String, Object> parameter = new HashMap<>();

        parameter.put("engine", "baidu");
        parameter.put("q", "Coffee");
        parameter.put("api_key", "f72a9b018803fb99c3ce3e790be533fd6d9226bd40597f25ecff83dc9a941d47");
        SearchApiWebSearchEngine webSearchEngine = SearchApiWebSearchEngine.builder()
                .apiKey(searchApiKey)
                .engine(searchEngine)
                //.optionalParameters(parameter)
                .build();
        return AiServices.builder(WebSearchAiService.class)
                .streamingChatModel(streamingChatModel)
                .tools(new WebSearchTool(webSearchEngine))
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
                //.autoFlushOnInsert(true)// 插入后自动刷新
                .idFieldName("id")                   // ID字段名（INT64，手动指定）
                .vectorFieldName("vector")           // 向量字段名（Float Vector）
                .textFieldName("text")               // 文本字段名（VarChar）
                .metadataFieldName("metadata")
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> milvusEmbeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(milvusEmbeddingStore)
                .embeddingModel(embeddingModel)
                .build();
    }
}
