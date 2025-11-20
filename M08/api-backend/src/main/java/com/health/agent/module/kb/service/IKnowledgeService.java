package com.health.agent.module.kb.service;

import com.health.agent.module.kb.vo.RAGQueryResultVO;

public interface IKnowledgeService {

    /**
     * 构建知识库：对长文进行分片并入库
     * @return 插入的片段数量
     */
    int buildKnowledge(String title,
                       String content,
                       String category,
                       String source,
                       String keywords,
                       int chunkSize,
                       int chunkOverlap);

    /**
     * 检索知识库并可选生成答案
     */
    RAGQueryResultVO queryKnowledge(String question,
                                    String category,
                                    int topK,
                                    boolean withAnswer);

    /**
     * 高级检索：支持模式（keyword/vector/hybrid）与权重（alpha/beta）
     */
    RAGQueryResultVO queryKnowledgeAdvanced(String question,
                                            String category,
                                            int topK,
                                            boolean withAnswer,
                                            String mode,
                                            Double alpha,
                                            Double beta);
}