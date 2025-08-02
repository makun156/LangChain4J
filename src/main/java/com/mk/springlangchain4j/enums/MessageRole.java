package com.mk.springlangchain4j.enums;

/**
 * 消息角色枚举
 */
public enum MessageRole {
    /**
     * 用户消息
     */
    USER("USER", "用户"),
    
    /**
     * AI消息
     */
    AI("AI", "AI助手");

    private final String code;
    private final String description;

    MessageRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举值
     */
    public static MessageRole fromCode(String code) {
        for (MessageRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown message role code: " + code);
    }
}