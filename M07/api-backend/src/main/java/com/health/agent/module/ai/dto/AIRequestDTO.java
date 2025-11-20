package com.health.agent.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI请求数据传输对象
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequestDTO {
    
    /**
     * 对话消息列表（支持多轮对话）
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
    
    /**
     * 模型名称（可选，默认使用配置中的模型）
     */
    private String model;
    
    /**
     * 最大生成token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数（0.0-1.0），控制回复的随机性
     * 值越高越随机，值越低越确定
     */
    private Double temperature;
    
    /**
     * Top-P采样参数（0.0-1.0）
     */
    private Double topP;
    
    /**
     * 是否使用流式响应
     */
    @Builder.Default
    private Boolean stream = false;
    
    /**
     * 用户ID（用于日志和监控）
     */
    private Long userId;
    
    /**
     * 会话ID（用于关联对话）
     */
    private Long sessionId;
    
    /**
     * 对话消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 消息角色：system/user/assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
    
    /**
     * 便捷方法：添加系统消息
     */
    public AIRequestDTO addSystemMessage(String content) {
        this.messages.add(Message.builder()
                .role("system")
                .content(content)
                .build());
        return this;
    }
    
    /**
     * 便捷方法：添加用户消息
     */
    public AIRequestDTO addUserMessage(String content) {
        this.messages.add(Message.builder()
                .role("user")
                .content(content)
                .build());
        return this;
    }
    
    /**
     * 便捷方法：添加助手消息
     */
    public AIRequestDTO addAssistantMessage(String content) {
        this.messages.add(Message.builder()
                .role("assistant")
                .content(content)
                .build());
        return this;
    }
}

