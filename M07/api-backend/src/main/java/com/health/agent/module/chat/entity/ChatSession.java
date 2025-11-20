package com.health.agent.module.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 * 对应数据库表：chat_session
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 会话类型
     * chat-普通聊天, assessment-评估
     */
    @Builder.Default
    private String type = "chat";
    
    /**
     * 状态
     * 0-已结束, 1-进行中
     */
    @Builder.Default
    private Integer status = 1;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 是否删除
     * 0-未删除, 1-已删除
     */
    @Builder.Default
    private Integer isDeleted = 0;
}

