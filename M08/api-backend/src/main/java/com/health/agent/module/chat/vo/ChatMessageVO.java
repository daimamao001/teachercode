package com.health.agent.module.chat.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息VO
 * 用于返回给前端的消息信息
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息信息")
public class ChatMessageVO {
    
    @Schema(description = "消息ID")
    private Long id;
    
    @Schema(description = "会话ID")
    private Long sessionId;
    
    @Schema(description = "角色：user-用户, assistant-AI")
    private String role;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "Token数量")
    private Integer tokens;
    
    @Schema(description = "使用的AI模型")
    private String model;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

