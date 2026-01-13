package com.mk.springlangchain4j.config;

import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.service.VectorAiService;
import com.mk.springlangchain4j.service.WebSearchAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class LangChain4JConfig {
    @Value("${bigmodel.api-key}")
    String apiKey;
    //@Bean
    //public ToolProvider mcpToolProvider(){
    //    McpTransport transport = new StdioMcpTransport.Builder()
    //            //.command(List.of("cmd", "/c", "npx","-y","@amap/amap-maps-mcp-server"))
    //            .command(List.of("cmd", "/c", "npx","-y","@baidumap/mcp-server-baidu-map"))
    //            .environment(Map.of("BAIDU_MAP_API_KEY","UMfVCwZ87JaNSrhQe1vrZrY4X888KniF"))
    //            //.logEvents(true)
    //            .build();
    //    McpClient mcpClient = new DefaultMcpClient.Builder()
    //            //.key("MyMCPClient")
    //            .transport(transport)
    //            .build();
    //    ToolProvider toolProvider = McpToolProvider.builder()
    //            .mcpClients(mcpClient)
    //            .build();
    //    return toolProvider;
    //}


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
    //public CustomAiService customAiService(ToolProvider mcpToolProvider,ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(CustomAiService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                //.toolProvider(mcpToolProvider)
                //.tools(new FuncCallingTools(),mcpToolProvider)
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

    //@Bean
    //public WebSearchAiService webSearchAiService(){
    //    SearchApiWebSearchEngine webSearchEngine = SearchApiWebSearchEngine.builder()
    //            .apiKey(searchApiKey)
    //            .engine(searchEngine)
    //            .build();
    //    return AiServices.builder(WebSearchAiService.class)
    //            .streamingChatModel(streamingChatModel)
    //            .tools(new WebSearchTool(webSearchEngine),new FuncCallingTools())
    //            .build();
    //}
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
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> milvusEmbeddingStore){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(milvusEmbeddingStore)
                .embeddingModel(embeddingModel)
                .build();
    }
}
