package com.mk.springlangchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface VectorAiService {

    Flux<String> streamingVectorChat(@MemoryId String memoryId, @UserMessage String message);

}
