package com.health.agent.module.kb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Knowledge {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String keywords;
    /** JSON字符串，预留向量嵌入 */
    private String embedding;
    private String source;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}