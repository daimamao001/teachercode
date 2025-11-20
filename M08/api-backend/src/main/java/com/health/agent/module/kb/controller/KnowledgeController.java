package com.health.agent.module.kb.controller;

import com.health.agent.common.api.ApiResponse;
import com.health.agent.module.kb.service.IKnowledgeService;
import com.health.agent.module.kb.client.RAGServiceClient;
import com.health.agent.module.kb.vo.RAGQueryResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/kb")
@Tag(name = "RAG知识库", description = "知识库构建与检索接口")
@Validated
@RequiredArgsConstructor
public class KnowledgeController {

    private final IKnowledgeService knowledgeService;
    private final RAGServiceClient ragServiceClient;

    @PostMapping("/build")
    @Operation(summary = "构建知识库", description = "将长文分片并写入知识库表")
    public ApiResponse<Integer> build(@Validated @RequestBody BuildKBRequest req) {
        log.info("知识库构建请求: title={}, category={}, source={}", req.getTitle(), req.getCategory(), req.getSource());
        int count = knowledgeService.buildKnowledge(
                req.getTitle(), req.getContent(), req.getCategory(), req.getSource(), req.getKeywords(),
                req.getChunkSize() != null ? req.getChunkSize() : 500,
                req.getChunkOverlap() != null ? req.getChunkOverlap() : 50
        );
        // 可选：同时写入向量库
        if (Boolean.TRUE.equals(req.getToVector())) {
            try {
                int ingested = ragServiceClient.ingestRaw(
                        req.getTitle(), req.getCategory(), req.getContent(), req.getKeywords(),
                        req.getChunkSize(), req.getChunkOverlap()
                );
                log.info("rag-service 向量入库完成，分片数={}", ingested);
            } catch (Exception e) {
                log.warn("rag-service 向量入库失败: {}", e.getMessage());
            }
        }
        return ApiResponse.ok(count);
    }

    @PostMapping("/query")
    @Operation(summary = "检索并可选生成答案", description = "全文/模糊检索拼接上下文，支持调用模型生成最终答案")
    public ApiResponse<RAGQueryResultVO> query(@Validated @RequestBody QueryKBRequest req) {
        log.info("知识库检索: question={}, category={}, topK={}, withAnswer={}, mode={}, alpha={}, beta={}",
                req.getQuestion(), req.getCategory(), req.getTopK(), req.getWithAnswer(), req.getMode(), req.getAlpha(), req.getBeta());
        RAGQueryResultVO vo = knowledgeService.queryKnowledgeAdvanced(
                req.getQuestion(), req.getCategory(),
                req.getTopK() != null ? req.getTopK() : 5,
                req.getWithAnswer() != null && req.getWithAnswer(),
                req.getMode(), req.getAlpha(), req.getBeta()
        );
        return ApiResponse.ok(vo);
    }

    @Data
    public static class BuildKBRequest {
        @NotBlank(message = "标题不能为空")
        @Parameter(description = "文章标题")
        private String title;

        @NotBlank(message = "内容不能为空")
        @Parameter(description = "文章内容（将被分片）")
        private String content;

        @NotBlank(message = "分类不能为空")
        @Parameter(description = "分类")
        private String category;

        @Parameter(description = "关键词（可选）")
        private String keywords;

        @Parameter(description = "来源（可选）")
        private String source;

        @Min(value = 100, message = "分片大小至少100")
        @Parameter(description = "分片大小（默认500）")
        private Integer chunkSize;

        @Min(value = 0, message = "重叠不得为负数")
        @Parameter(description = "分片重叠（默认50）")
        private Integer chunkOverlap;

        @Parameter(description = "是否写入向量库（默认false）")
        private Boolean toVector;
    }

    @Data
    public static class QueryKBRequest {
        @NotBlank(message = "问题不能为空")
        @Parameter(description = "用户问题/检索词")
        private String question;

        @Parameter(description = "分类过滤（可选）")
        private String category;

        @Min(value = 1, message = "topK至少为1")
        @Parameter(description = "返回条数（默认5）")
        private Integer topK;

        @Parameter(description = "是否调用模型生成答案（默认true）")
        private Boolean withAnswer;

        @Parameter(description = "检索模式：keyword/vector/hybrid（默认keyword）")
        private String mode;

        @Parameter(description = "混合检索语义权重alpha（默认0.7）")
        private Double alpha;

        @Parameter(description = "混合检索关键词权重beta（默认0.3）")
        private Double beta;
    }
}