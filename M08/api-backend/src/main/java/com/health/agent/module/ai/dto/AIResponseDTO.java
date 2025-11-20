package com.health.agent.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI响应数据传输对象
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {
    
    /**
     * AI回复内容
     */
    private String content;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * Token使用情况
     */
    private Usage usage;
    
    /**
     * 请求ID（用于问题追踪）
     */
    private String requestId;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 是否成功
     */
    @Builder.Default
    private Boolean success = true;
    
    /**
     * 错误消息（如果失败）
     */
    private String errorMessage;
    
    /**
     * Token使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 提示词token数
         */
        private Integer promptTokens;
        
        /**
         * 生成token数
         */
        private Integer completionTokens;
        
        /**
         * 总token数
         */
        private Integer totalTokens;
    }
    
    /**
     * 创建成功响应
     */
    public static AIResponseDTO success(String content) {
        return AIResponseDTO.builder()
                .success(true)
                .content(content)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static AIResponseDTO failure(String errorMessage) {
        return AIResponseDTO.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}

