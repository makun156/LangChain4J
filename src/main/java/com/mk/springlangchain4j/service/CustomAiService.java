package com.mk.springlangchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface CustomAiService {
    String chat(String message);

    Flux<String> streamingChat(String message);

    Flux<String> streamingChat(@MemoryId String memoryId, @UserMessage String message);
}
