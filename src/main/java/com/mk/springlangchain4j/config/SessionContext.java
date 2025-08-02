package com.mk.springlangchain4j.config;

import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    // 线程局部变量存储当前会话ID
    private static final ThreadLocal<String> CURRENT_SESSION_ID = new ThreadLocal<>();

    // 设置当前会话ID
    public void setSessionId(String sessionId) {
        CURRENT_SESSION_ID.set(sessionId);
    }

    // 获取当前会话ID
    public String getSessionId() {
        return CURRENT_SESSION_ID.get();
    }

    // 清除当前线程的会话ID（避免内存泄漏）
    public void clear() {
        CURRENT_SESSION_ID.remove();
    }
}
