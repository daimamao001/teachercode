package com.health.agent.module.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息DTO
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送消息请求")
public class SendMessageDTO {
    
    @Schema(description = "会话ID", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    @Schema(description = "消息内容", required = true, example = "我最近感觉压力很大")
    @NotBlank(message = "消息内容不能为空")
    private String content;
}

