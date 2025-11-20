package com.health.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 服务配置（Python微服务）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RAGConfig {
    /** 基础服务地址，如：http://localhost:8801 */
    private String serviceBaseUrl = "http://localhost:8801";

    /** 请求超时时间（毫秒） */
    private Long timeoutMs = 3000L;

    /** 混合检索默认权重（语义向量权重） */
    private Double alpha = 0.7;

    /** 混合检索默认权重（关键词权重） */
    private Double beta = 0.3;
}