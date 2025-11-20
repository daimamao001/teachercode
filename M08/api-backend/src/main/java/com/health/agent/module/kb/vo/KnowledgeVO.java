package com.health.agent.module.kb.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库返回视图对象（仅必要字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeVO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String keywords;
    private String source;
}