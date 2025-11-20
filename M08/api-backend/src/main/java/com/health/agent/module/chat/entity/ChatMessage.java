package com.health.agent.module.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 * 对应数据库表：chat_message
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 角色
     * user-用户, assistant-AI助手
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * Token数量
     * 记录该消息消耗的token数
     */
    private Integer tokens;
    
    /**
     * 使用的AI模型
     * 例如：hunyuan-turbos-latest
     */
    private String model;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 便捷方法：判断是否为用户消息
     */
    public boolean isUserMessage() {
        return "user".equals(this.role);
    }
    
    /**
     * 便捷方法：判断是否为AI消息
     */
    public boolean isAssistantMessage() {
        return "assistant".equals(this.role);
    }
}

