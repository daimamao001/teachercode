package com.health.agent.module.kb.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG查询结果：匹配项、构建的上下文、以及可选答案
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGQueryResultVO {
    private List<KnowledgeVO> matches;
    private String context;
    private String answer;
}